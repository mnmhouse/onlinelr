package org.example;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.dataproc.SplitStreamOp;
import com.alibaba.alink.operator.stream.evaluation.EvalBinaryClassStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlPredictStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlTrainStreamOp;
import com.alibaba.alink.operator.stream.source.BaseSourceStreamOp;
import com.alibaba.alink.operator.stream.source.CsvSourceStreamOp;
import com.alibaba.alink.operator.stream.source.KafkaSourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.dataproc.StandardScaler;
import com.alibaba.alink.pipeline.feature.FeatureHasher;

public class FTRLExample {

    public static void main(String[] args) throws Exception {

        String schemaStr
                = "id string, click string, dt string, C1 string, banner_pos int, site_id string, site_domain string, "
                + "site_category string, app_id string, app_domain string, app_category string, device_id string, "
                + "device_ip string, device_model string, device_type string, device_conn_type string, C14 int, C15 int, "
                + "C16 int, C17 int, C18 int, C19 int, C20 int, C21 int";
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                .setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-small.csv")
                .setSchemaStr(schemaStr);

        String labelColName = "click";
        String[] selectedColNames = new String[]{
                "C1", "banner_pos", "site_category", "app_domain",
                "app_category", "device_type", "device_conn_type",
                "C14", "C15", "C16", "C17", "C18", "C19", "C20", "C21",
                "site_id", "site_domain", "device_id", "device_model"};
        String[] categoryColNames = new String[]{
                "C1", "banner_pos", "site_category", "app_domain",
                "app_category", "device_type", "device_conn_type",
                "site_id", "site_domain", "device_id", "device_model"};
        String[] numericalColNames = new String[]{
                "C14", "C15", "C16", "C17", "C18", "C19", "C20", "C21"};


        String vecColName = "vec";
        int numHashFeatures = 30000;


        // part1 ??????????????????
        // 1  ???????????????????????????
        Pipeline featurePipeline = new Pipeline()
                .add(
                        new StandardScaler()
                                .setSelectedCols(numericalColNames)
                )
                .add(
                        new FeatureHasher()
                                .setSelectedCols(selectedColNames)
                                .setCategoricalCols(categoryColNames)
                                .setOutputCol(vecColName)
                                .setNumFeatures(numHashFeatures)
                );
        //  fit feature pipeline model
        PipelineModel featurePipelineModel = featurePipeline.fit(trainBatchData);


        // ???????????????????????????????????????FTRL???????????????????????????????????????????????????????????????
        LogisticRegressionTrainBatchOp lr = new LogisticRegressionTrainBatchOp()
                .setVectorCol(vecColName)
                .setLabelCol(labelColName)
                .setWithIntercept(true)
                .setMaxIter(10);
        BatchOperator<?> initModel = featurePipelineModel.transform(trainBatchData).link(lr);



        // part 2 ??????????????????

        // ?????????????????????????????????????????? ?????????????????????Kafka???????????????
        BaseSourceStreamOp  data = getSourceStreamOp(schemaStr);

        // ?????????????????????????????????????????????????????????????????????
        SplitStreamOp splitter = new SplitStreamOp().setFraction(0.5).linkFrom(data);

        // ftrl train ??????????????????????????????FTRL????????????
        FtrlTrainStreamOp model = new FtrlTrainStreamOp(initModel)
                .setVectorCol(vecColName)
                .setLabelCol(labelColName)
                .setWithIntercept(true)
                .setAlpha(0.1)
                .setBeta(0.1)
                .setL1(0.01)
                .setL2(0.01)
                .setTimeInterval(10)
                .setVectorSize(numHashFeatures)
                .linkFrom(featurePipelineModel.transform(splitter));

        // ftrl predict ???FTRL?????????????????????????????????????????????????????????
        FtrlPredictStreamOp predictResult = new FtrlPredictStreamOp(initModel)
                .setVectorCol(vecColName)
                .setPredictionCol("pred")
                .setReservedCols(new String[]{labelColName})
                .setPredictionDetailCol("details")
                .linkFrom(model, featurePipelineModel.transform(splitter.getSideOutput(0)));


        // ftrl eval ??????????????????????????????
        predictResult
                .link(
                        new EvalBinaryClassStreamOp()
                                .setLabelCol(labelColName)
                                .setPredictionCol("pred")
                                .setPredictionDetailCol("details")
                                .setTimeInterval(10)
                )
                .link(
                        new JsonValueStreamOp()
                                .setSelectedCol("Data")
                                .setReservedCols(new String[]{"Statistics"})
                                .setOutputCols(new String[]{"Accuracy", "AUC", "ConfusionMatrix"})
                                .setJsonPath(new String[]{"$.Accuracy", "$.AUC", "$.ConfusionMatrix"})
                )
                .print();
    }

    private static BaseSourceStreamOp<CsvSourceStreamOp> getSourceStreamOp(String schemaStr) {
        // ????????????????????????????????????????????????????????????Kafka ????????????
        CsvSourceStreamOp data = new CsvSourceStreamOp()
                .setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-ctr-train-8M.csv")
               // .setFilePath("/Users/haozhang/Downloads/avazu-ctr-train-8M.csv")
                .setSchemaStr(schemaStr)
                .setIgnoreFirstLine(true);
        return data;
    }

    private static BaseSourceStreamOp<KafkaSourceStreamOp> getKafakSourceStreamOp(String schemaStr) {
        // ????????????????????????????????????????????????????????????Kafka ????????????
        KafkaSourceStreamOp soure = new KafkaSourceStreamOp()
                .setBootstrapServers("localhost:9092")
                .setTopic("train_data_topic")
                .setStartupMode("EARLIEST")
                .setGroupId("");

        return soure;
    }

}