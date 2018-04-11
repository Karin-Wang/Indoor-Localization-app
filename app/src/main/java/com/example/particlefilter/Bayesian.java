package com.example.particlefilter;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.InputStreamReader;
import java.io.InputStream;
//import android.content;

/**
 * Created by Kaering on 3/30/18.
 */

public class Bayesian {

    Map<String, float[][]> radioMap = new HashMap<>();

    Map<String, float[][]> radioMapN = new HashMap<>();
    Map<String, float[][]> radioMapE = new HashMap<>();
    Map<String, float[][]> radioMapS = new HashMap<>();
    Map<String, float[][]> radioMapW = new HashMap<>();
    Map<String, float[][]>[] radioMap_All = new Map[]{radioMapN, radioMapE, radioMapS, radioMapW};


    private double threshold = 0.9;
    private int maxIter = 10;
    String filename = "r";

    int predict;
    double[][] prior;
    double[][] post;
    int iteration;
    int j;
    double proba;

//    public Bayesian(){
//        this.getRadioMap();
//        this.initialize();
//    }


    public void getRadioMap(BufferedReader reader){
        try {
            String line = null;
            while((line=reader.readLine())!=null){
                String ap = line;
                float[][] curr = new float[19][2];
                for (int i=0; i<19; i++){
                    line=reader.readLine();
                    String[] item = line.split("，");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                    String[] a = item[0].split(",");

                    curr[i][0] = Float.parseFloat(a[0].substring(1));
                    curr[i][1] = Float.parseFloat(a[1].substring(0,a[1].length()-1));
                }
                this.radioMap.put(ap, curr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllRadioMap(BufferedReader[] readers) {
        int count = 0;
        for (BufferedReader reader : readers) {
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    String ap = line;
                    float[][] curr = new float[19][2];
                    for (int i = 0; i < 19; i++) {
                        line = reader.readLine();
                        String[] item = line.split("，");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                        String[] a = item[0].split(",");

                        curr[i][0] = Float.parseFloat(a[0].substring(1));
                        curr[i][1] = Float.parseFloat(a[1].substring(0, a[1].length() - 1));
                    }
                    this.radioMap_All[count].put(ap, curr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
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
//                Log.d("pdf", String.valueOf(a2[i]));
//                Log.d("post", String.valueOf(res[i]));
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
            for (int j = 0; j < 19; j++) prior[i][j] = 1/19.0;
//            Log.d("priorrrrr", String.valueOf(prior[i][j]));
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
        int sizet = 30;
        if (test_data.size()<30){
            sizet = test_data.size();
        }
        for (int item = 0; item < sizet; item++) {
            double[] pdf = new double[19];
            for (int i = 0; i < 19; i++) {
                pdf[i] = 0;
            }
            for (int cell = 0; cell < 19; cell++) {
                String AP = sorted_test_data.get(item).getKey();
                if (radioMap.containsKey(AP)) {
                    if (radioMap.get(AP)[cell][1] == 0.0) {
                        pdf[cell] = 0.0;
                    } else {
                        NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell][0], radioMap.get(AP)[cell][1]);
                        pdf[cell] = d.density(normalization((test_data.get(AP))));
                        if (Double.isNaN(pdf[cell])) {
                            pdf[cell] = 0.0;
                        }
                    }
                }
                else{
                    pdf[cell] = 0.0;
                }
//                Log.d("proba1", String.valueOf(pdf[cell]));
            }
            post[j] = matrixOperation(prior[j], pdf, "*");

            overall_post = matrixOperation(post[j], overall_post, "+");
            j += 1;
        }
        double sum1 = 0;
        for (int i = 0; i < 19; i++) {
            sum1 += overall_post[i];
//            Log.d("overall", String.valueOf(sum1));
        }
        double[] average_overvall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            average_overvall_post[i] = overall_post[i] / sum1;
//            Log.d("average_overvall_post", String.valueOf(average_overvall_post[i]));
        }
        Object[] fdmax = findMax(average_overvall_post);
        predict = (int) fdmax[1];
        proba = (double) fdmax[0];
        return fdmax;
    }

    public Object[] bayes_new(Map<String, Integer> test_data) {
        prior = post.clone();
        j = 0;
        double[] overall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            overall_post[i] = 0;
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        int sizet = 30;
        if (test_data.size()<30){
            sizet = test_data.size();
        }
        for (int item = 0; item < sizet; item++) {
            double[] pdf = new double[19];
            for (int i = 0; i < 19; i++) {
                pdf[i] = 0;
            }
            for (int cell = 0; cell < 19; cell++) {
                String AP = sorted_test_data.get(item).getKey();
                if (radioMap.get(AP)[cell][1] == 0.0){
                    pdf[cell] = 0.0;
                }
                else {
                    NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell][0], radioMap.get(AP)[cell][1]);
                    pdf[cell] = d.density(normalization((test_data.get(AP))));
                    if (Double.isNaN(pdf[cell])) {
                        pdf[cell] = 0.0;
                    }
                }
//                Log.d("proba1", String.valueOf(pdf[cell]));
            }
            post[j] = matrixOperation(prior[j], pdf, "*");

            overall_post = matrixOperation(post[j], overall_post, "+");
            j += 1;
        }
        double sum1 = 0;
        for (int i = 0; i < 19; i++) {
            sum1 += overall_post[i];
//            Log.d("overall", String.valueOf(sum1));
        }
        double[] average_overvall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            average_overvall_post[i] = overall_post[i] / sum1;
//            Log.d("average_overvall_post", String.valueOf(average_overvall_post[i]));
        }
        Object[] fdmax = findMax(average_overvall_post);
        predict = (int) fdmax[1];
        proba = (double) fdmax[0];
        return fdmax;
    }

    public int findMax1(double[] arr){
        int res = -1;
        double max = arr[0];
        int pred = 0;
        for (int i=0; i<arr.length; i++){
            if (max < arr[i]){
                max = arr[i];
                pred = i;
            }
        }
//        res[0] = max;
        res = pred+1;
        return res;
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
            res[1] = pred+1;
            return res;
        }

//    public void chooseRadioMap(double angle){
//
//        // double initialAngle = MyView.getInitialAngle();
//
//        double Q1 = -1.41; // value at pi/8
//        double Q2 = 0.15; // value at 3/pi*8
//        double Q3 = 1.72; // value at 5*pi/8
//
//        if (angle > -Math.PI && angle < Q1){
//            radioMap = radioMapN;
//            Log.d("North", "");
//        } else if (angle > Q1 && angle < Q2){
//            radioMap = radioMapE;
//            Log.d("East", "");
//        } else if (angle > Q2 && angle < Q3){
//            radioMap = radioMapS;
//            Log.d("South", "");
//        }
//
//    }

}



