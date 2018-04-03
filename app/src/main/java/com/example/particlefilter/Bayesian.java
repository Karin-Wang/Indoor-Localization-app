package com.example.particlefilter;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

/**
 * Created by Kaering on 3/30/18.
 */

public class Bayesian {

    Map<String, float[][]> radioMap;
    private double threshold = 0.9;
    private int maxIter = 10;
    String filename = "r";

    private int predict;
    double[][] prior;
    double[][] post;
    int iteration;
    int j;
    double proba;


    public void getRadioMap(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(R.raw.radio_map)));//换成你的文件名
//            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null;
            while((line=reader.readLine())!=null){
                String ap = line;
                float[][] curr = new float[19][2];
                for (int i=0; i<19; i++){
                    line=reader.readLine();
                    String[] item = line.split("，");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                    curr[i][0] = Float.parseFloat(item[0]);
                    curr[i][1] = Float.parseFloat(item[1]);
                }
                this.radioMap.put(ap, curr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int normalization(int data){
        data = (int)((data+80)*255/30);
        return data;
    }

    public List<Entry<String, Integer>> sortMap(Map<String, Integer> map){
        Set<Entry<String, Integer>> set = map.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        return list;
    }

    public double[] matrixOperation(double[] a1, double[] a2, String op){
        double[] res = new double[a1.length];
        if (op.equals("*")){
            for (int i = 0; i < a1.length; i++){
                res[i] = a1[i] * a2[i];
            }
        }
        else if(op.equals("+")){
            for (int i = 0; i < a1.length; i++){
                res[i] = a1[i] + a2[i];
            }
        }
        return res;
    }

    public void initialize(){
        predict = -1;
        prior = new double[this.radioMap.keySet().size()][19];
        for (int i = 0; i<this.radioMap.keySet().size(); i++) {
            for (int j = 0; j < 19; j++) {
                prior[i][j] = 1 / 19;
            }
        }
        post = prior.clone();
        iteration = 0;
        proba = 0;
    }


    public Object[] bayes(Map<String, Integer> test_data) {
        prior = post.clone();
        j = 0;
        double[] overall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            overall_post[i] = 0;
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        for (int item = 0; item < 30; item++) {
            double[] pdf = new double[19];
            for (int i = 0; i < 19; i++) {
                pdf[i] = 0;
            }
            for (int cell = 0; cell < 19; cell++) {
                String AP = sorted_test_data.get(item).getKey();
                NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell][0], radioMap.get(AP)[cell][1]);
                pdf[cell] = d.density(normalization((test_data.get(AP))));
                if (Double.isNaN(pdf[cell])) {
                    pdf[cell] = 0.0;
                }
            }
            post[j] = matrixOperation(prior[j], pdf, "*");
            overall_post = matrixOperation(post[j], overall_post, "+");
            j += 1;
        }
        double sum1 = 0;
        for (int i = 0; i < 19; i++) {
            sum1 += overall_post[i];
        }
        double[] average_overvall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            average_overvall_post[i] = average_overvall_post[i] / sum1;
        }
        Object[] fdmax = findMax(average_overvall_post);
//        predict = (int) fdmax[1];
//        proba = (double) fdmax[0];
////            i++; //下一组数据
//        iteration++;
//        if (iteration > 10) {
//            return fdmax;
//        }
        return fdmax;
    }

        public Object[] findMax(double[] arr){
            Object[] res = new Object[2];
            double max = arr[0];
            int pred = 0;
            for (int i=0; i<arr.length; i++){
                if (max < arr[i]){
                    max = arr[i];
                    pred = i;
                }
            }
            res[0] = max;
            res[1] = pred;
            return res;
        }


    }



