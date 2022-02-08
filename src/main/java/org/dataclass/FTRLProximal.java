package org.dataclass;

/**
 * @Description: 模型更新类
 * @author: house.zhang
 * @date: 2022/2/7 13:46
 */
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FTRLProximal {

    // parameters->alpha, beta, l1, l2, dimensions
    private FTRLParameters parameters;
    // n->squared sum of past gradients
    public double[] n;
    // z->weights
    public double[] z;
    // w->lazy weights
    public Map<Integer, Double> w;

    public double[] n_;
    public double[] z_;
    public Map<Integer, Double> w_;

    public FTRLProximal(FTRLParameters parameters) {
        this.parameters = parameters;
        this.n = new double[parameters.dataDimensions];
        this.z = new double[parameters.dataDimensions];
        this.w = null;
    }


    /** x->p(y=1|x; w) , get w, nothing is changed*/
    public double  predict(Map<Integer, Double> x) {
        w = new HashMap<Integer, Double>();
        double decisionValue = 0.0;
        for (Entry<Integer, Double> e : x.entrySet()) {
            double sgn = sign(z[e.getKey()]);
            double weight = 0.0;
            if (sgn * z[e.getKey()] <= parameters.L1_lambda) {
                w.put(e.getKey(), weight);
            } else {
                weight = (sgn * parameters.L1_lambda - z[e.getKey()])
                        / ((parameters.beta + Math.sqrt(n[e.getKey()]))
                        / parameters.alpha + parameters.L2_lambda);
                w.put(e.getKey(), weight);
            }
            decisionValue += e.getValue() * weight;
        }
        decisionValue = Math.max(Math.min(decisionValue, 35.), -35.);
        return 1. / (1. + Math.exp(-decisionValue));
    }

    /** input: sample x, probability p, label y(-1(or 0) or 1)
     *  used: w
     *  update: n, z*/
    public void updateModel(Map<Integer, Double> x, double p, double y) {
        for(Entry<Integer, Double> e : x.entrySet()) {
            double grad = p * e.getValue();
            if(y == 1.0) {
                grad = (p - y) * e.getValue();
            }
            double sigma = (Math.sqrt(n[e.getKey()] + grad * grad) -
                    Math.sqrt(n[e.getKey()])) / parameters.alpha;
            z[e.getKey()] += (grad - sigma * w.get(e.getKey()));
            n[e.getKey()] += grad * grad;
        }
    }

    /**
     * N、Z、W
     * 模型参数保存函数
     * */
    public void saveModel(String filePath) throws IOException {

        String n_=String.valueOf(n[0]);
        String z_=String.valueOf(z[0]);
        String w_=String.valueOf(w.get(0));

        for(int i=1;i<n.length;i++){
            n_ = n_+" "+String.valueOf(n[i]);
            z_ = z_+" "+String.valueOf(z[i]);
            w_ = w_+" "+String.valueOf(w.get(i));
        }

        try{
            File file = new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(n_+"\r\n");
            bufferWriter.write(z_+"\r\n");
            bufferWriter.write(w_);
            bufferWriter.close();
            System.out.print("Done");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void loadModel(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
        String line = null;
        String[][] Str = new String[3][];
        int i = 0;
        while((line = br.readLine()) != null) {
            Str[i] = line.split(" ");
            i++;
        }
        n = new double[n.length];
        z = new double[z.length];
        w = new HashMap<Integer, Double>();
        for(int j=0;j<n.length;j++){
            n[j] = Double.valueOf(Str[0][j]);
            z[j] = Double.valueOf(Str[1][j]);
            w.put(j,Double.valueOf(Str[2][j]));
        }
    }


    public double predict_(Map<Integer, Double> x) {

        double decisionValue = 0.0;
        for (Entry<Integer, Double> e : x.entrySet()) {

            decisionValue += e.getValue() * w_.get(e.getKey());
        }
        decisionValue = Math.max(Math.min(decisionValue, 35.), -35.);
        return 1. / (1. + Math.exp(-decisionValue));
    }



    private double sign(double x) {
        if (x > 0) {
            return 1.0;
        } else if (x < 0) {
            return -1.0;
        } else {
            return 0.0;
        }
    }
}
