package org.dataclass;

/**
 * @Description: 模型参数类
 * @author: house.zhang
 * @date: 2022/2/7 13:40
 */

public class FTRLParameters {
    public double alpha;//学习速率参数
    public double beta;//调整参数，值为1时效果较好，无需调整
    public double L1_lambda;//L1范式参数
    public double L2_lambda;//L2范式参数
    public int dataDimensions;//数据特征维度数
    public int testDataSize;//测试集分次处理每次处理的个数
    public int interval;//每间隔interval进行一次打印
    public String modelPath;//模型训练参数的存放路径

    public FTRLParameters(double alpha, double beta,
                          double L1, double L2, int dataDimensions,int testDataSize, int interval,String modelPath) {
        this.alpha = alpha;
        this.beta = beta;
        this.L1_lambda = L1;
        this.L2_lambda = L2;
        this.dataDimensions = dataDimensions;
        this.testDataSize = testDataSize;
        this.interval = interval;
        this.modelPath = modelPath;
    }
}
