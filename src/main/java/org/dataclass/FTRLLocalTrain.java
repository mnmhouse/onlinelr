package org.dataclass;

/**
 * @Description:
 * @author: house.zhang
 * @date: 2022/2/7 13:44
 */
import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class FTRLLocalTrain {
    private FTRLProximal learner;
    private FTRLModelLoad mload;
    private LogLossEvalutor evalutor;
    private int printInterval;

    public FTRLLocalTrain(FTRLModelLoad mload, FTRLProximal learner, LogLossEvalutor evalutor, int interval) {
        this.mload = mload;
        this.learner = learner;
        this.evalutor = evalutor;
        this.printInterval = interval;
    }

    /**
     * 训练方法
     * */
    public void train(String modelPath,double[][] X,double[] Y) throws IOException {
        int trainedNum = 0;
        double totalLoss = 0.0;//损失值
        long startTime = System.currentTimeMillis();
        BufferedReader mp = new BufferedReader(new InputStreamReader(new FileInputStream(new File(modelPath)), "UTF-8"));
        String line = null;
        while((line = mp.readLine())!=null){
            learner.loadModel(modelPath);
        }
        for(int j=0;j<X.length;j++){
            Map<Integer, Double> x = new TreeMap<Integer, Double>();
            for (int i = 0; i < X[0].length; i++) {
                x.put(i, X[j][i]);
            }
            double y = ((int)Y[j] == 1) ? 1. : 0.;
            double p = learner.predict(x);
            learner.updateModel(x, p, y);
            double loss = LogLossEvalutor.calLogLoss(p, y);
            evalutor.addLogLoss(loss);
            totalLoss += loss;
            trainedNum += 1;
            if (trainedNum % printInterval == 0) {
                long currentTime = System.currentTimeMillis();
                double minutes = (double) (currentTime - startTime) / 60000;
                System.out.printf("%.3f, %.5f\n", minutes, evalutor.getAverageLogLoss());
            }
        }
        learner.saveModel(modelPath);
        System.out.printf("global average loss: %.5f\n", totalLoss / trainedNum);
    }

    public static void main(String[] args) {

    }
}