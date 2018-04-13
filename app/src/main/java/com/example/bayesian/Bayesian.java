package com.example.bayesian;

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

    //    Map<String, float[][]> radioMapN = new HashMap<>();
//    Map<String, float[][]> radioMapE = new HashMap<>();
//    Map<String, float[][]> radioMapS = new HashMap<>();
//    Map<String, float[][]> radioMapW = new HashMap<>();
    Map<String, float[][]>[] radioMap_All = new Map[4];


    private double threshold = 0.9;
    private int maxIter = 10;
    String filename = "r";

    private int predict;
    double[][] prior;
    double[][] post;
    int iteration;
    int j;
    double proba;

    double[][][] prior_all;
    double[][][] post_all;
    double[] proba_all;

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
                    if (a[1].substring(0,a[1].length()-1) == "0.0\""){
                        curr[i][1] = (float) 0.0;
                    }
                    else {
                        curr[i][1] = Float.parseFloat(a[1].substring(0, a[1].length() - 1));
                    }
//                    Log.d("variance", String.valueOf(curr[i][1]));
                }
                this.radioMap.put(ap, curr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllRadioMap(BufferedReader[] readers) {
        for (int i = 0; i<4; i++){
            this.radioMap_All[i] = new HashMap<>();
        }
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
                Log.d("radio_map", String.valueOf(radioMap_All[count].keySet().size()));
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

    public void initaialize_new(){
        prior_all = new double[4][][];
        post_all = new double[4][][];
        for(int c = 0; c < 4; c++) {
            prior_all[c] = new double[this.radioMap_All[c].keySet().size()][19];
            for (int i = 0; i<this.radioMap_All[c].keySet().size(); i++) {
                for (int j = 0; j < 19; j++) prior_all[c][i][j] = 1/19.0;
            }
            post_all[c] = prior_all[c].clone();
            proba_all = new double[]{0.0, 0.0, 0.0, 0.0};
            iteration = 0;
        }
    }

    public void initialize_opt(){
        predict = -1;
        prior = new double[this.radioMap.keySet().size()][3];
        for (int i = 0; i<this.radioMap.keySet().size(); i++) {
            for (int j = 0; j < 3; j++) prior[i][j] = 1/3.0;
//            Log.d("priorrrrr", String.valueOf(prior[i][j]));
        }
        post = prior.clone();
        iteration = 0;
        proba = 0;
    }

    public void initialize_opt1(){
        predict = -1;
        prior = new double[this.radioMap.keySet().size()][4];
        for (int i = 0; i<this.radioMap.keySet().size(); i++) {
            for (int j = 0; j < 4; j++) prior[i][j] = 1/4.0;
//            Log.d("priorrrrr", String.valueOf(prior[i][j]));
        }
        post = prior.clone();
        iteration = 0;
        proba = 0;
    }
    public Object[] bayes_optimize(Map<String, Integer> test_data, int[] zone) {
        prior = post.clone();
        j = 0;
        double[] overall_post = new double[3];
        for (int i = 0; i < 3; i++) {
            overall_post[i] = 0;
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        int sizet = 35;
        if (test_data.size()<35){
            sizet = test_data.size();
        }
        for (int item = 0; item < sizet; item++) {
            double[] pdf = new double[3];
            for (int i = 0; i < 3; i++) {
                pdf[i] = 0;
            }
            int c = 0;
            for (int cell : zone) {
                String AP = sorted_test_data.get(item).getKey();
                if (radioMap.containsKey(AP)) {
                    if (radioMap.get(AP)[cell-1][1] == 0.0) {
                        pdf[c] = 0.0;
                    } else {
                        if (radioMap.get(AP)[cell-1][1] <= 0.0){
                            pdf[c] = 0.0;
                        }
                        else {
                            NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell-1][0], radioMap.get(AP)[cell-1][1]);
                            pdf[c] = d.density(normalization((test_data.get(AP))));
                            if (Double.isNaN(pdf[c])) {
                                pdf[c] = 0.0;
                            }
                        }
                    }
//                Log.d("proba1", String.valueOf(pdf[cell]));
                }
                else{
                    pdf[c] = 0.0;
                }
                c++;
            }
            post[j] = matrixOperation(prior[j], pdf, "*");

            overall_post = matrixOperation(post[j], overall_post, "+");
            j += 1;
        }
        double sum1 = 0;
        for (int i = 0; i < 3; i++) {
            sum1 += overall_post[i];
//            Log.d("overall", String.valueOf(sum1));
        }
        double[] average_overvall_post = new double[3];
        for (int i = 0; i < 3; i++) {
            average_overvall_post[i] = overall_post[i] / sum1;
//            Log.d("average_overvall_post", String.valueOf(average_overvall_post[i]));
        }
        Object[] fdmax = findMax(average_overvall_post);
        predict = (int) fdmax[1];
        proba = (double) fdmax[0];
        return fdmax;
    }

    public Object[] bayes_optimize1(Map<String, Integer> test_data, int[] zone) {
        prior = post.clone();
        j = 0;
        double[] overall_post = new double[4

                ];
        for (int i = 0; i < 4; i++) {
            overall_post[i] = 0;
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        int sizet = 35;
        if (test_data.size()<35){
            sizet = test_data.size();
        }
        for (int item = 0; item < sizet; item++) {
            double[] pdf = new double[4];
            for (int i = 0; i < 4; i++) {
                pdf[i] = 0;
            }
            int c = 0;
            for (int cell : zone) {
                String AP = sorted_test_data.get(item).getKey();
                if (radioMap.containsKey(AP)) {
                    if (radioMap.get(AP)[cell-1][1] == 0.0) {
                        pdf[c] = 0.0;
                    } else {
                        if (radioMap.get(AP)[cell-1][1] <= 0.0){
                            pdf[c] = 0.0;
                        }
                        else {
                            NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell-1][0], radioMap.get(AP)[cell-1][1]);
                            pdf[c] = d.density(normalization((test_data.get(AP))));
                            if (Double.isNaN(pdf[c])) {
                                pdf[c] = 0.0;
                            }
                        }
                    }
//                Log.d("proba1", String.valueOf(pdf[cell]));
                }
                else{
                    pdf[c] = 0.0;
                }
                c++;
            }
            post[j] = matrixOperation(prior[j], pdf, "*");

            overall_post = matrixOperation(post[j], overall_post, "+");
            j += 1;
        }
        double sum1 = 0;
        for (int i = 0; i < 4; i++) {
            sum1 += overall_post[i];
//            Log.d("overall", String.valueOf(sum1));
        }
        double[] average_overvall_post = new double[4];
        for (int i = 0; i < 4; i++) {
            average_overvall_post[i] = overall_post[i] / sum1;
//            Log.d("average_overvall_post", String.valueOf(average_overvall_post[i]));
        }
        Object[] fdmax = findMax(average_overvall_post);
        predict = (int) fdmax[1];
        proba = (double) fdmax[0];
        return fdmax;
    }


    public Object[] bayes(Map<String, Integer> test_data) {
        prior = post.clone();
        j = 0;
        double[] overall_post = new double[19];
        for (int i = 0; i < 19; i++) {
            overall_post[i] = 0;
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        int sizet = 35;
        if (test_data.size()<35){
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
                        if (radioMap.get(AP)[cell][1] <= 0.0){
                            pdf[cell] = 0.0;
                        }
                        else {
                            NormalDistribution d = new NormalDistribution(radioMap.get(AP)[cell][0], radioMap.get(AP)[cell][1]);
                            pdf[cell] = d.density(normalization((test_data.get(AP))));
                            if (Double.isNaN(pdf[cell])) {
                                pdf[cell] = 0.0;
                            }
                        }
                    }
//                Log.d("proba1", String.valueOf(pdf[cell]));
                }
                else{
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

    public Object[][] bayes_new(Map<String, Integer> test_data) {
        prior_all = post_all.clone();
        j = 0;
        double[][] overall_post_all = new double[4][19];
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 19; i++) {
                overall_post_all[j][i] = 0;
            }
        }
        List<Entry<String, Integer>> sorted_test_data = sortMap(test_data);
        int sizet = 30;
        if (test_data.size() < 30) {
            sizet = test_data.size();
        }
        for (int item = 0; item < sizet; item++) {
            double[][] pdf_all = new double[4][19];
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 19; i++) {
                    pdf_all[j][i] = 0;
                }
            }
            for (int cell = 0; cell < 19; cell++) {
                String AP = sorted_test_data.get(item).getKey();
                for (int i = 0; i < 4; i++) {
                    Map<String, float[][]> rm = radioMap_All[i];
                    if (rm.containsKey(AP)) {
                        if (rm.get(AP)[cell][1] == 0.0) {
                            pdf_all[i][cell] = 0.0;
                        } else {
                            NormalDistribution d = new NormalDistribution(rm.get(AP)[cell][0], rm.get(AP)[cell][1]);
                            pdf_all[i][cell] = d.density(normalization((test_data.get(AP))));
                            if (Double.isNaN(pdf_all[i][cell])) {
                                pdf_all[i][cell] = 0.0;
                            }
                        }
                    } else {
                        pdf_all[i][cell] = 0.0;
                    }
                }
//                Log.d("proba1", String.valueOf(pdf[cell]));
            }
            for (int i = 0; i < 4; i++) {

                post_all[i][j] = matrixOperation(prior_all[i][j], pdf_all[i], "*");

                overall_post_all[i] = matrixOperation(post_all[i][j], overall_post_all[i], "+");
            }
            j += 1;
        }

        double[] sum1_all = {0, 0, 0, 0};
        for (int a = 0; a < 4; a++) {
            for (int i = 0; i < 19; i++) {
                sum1_all[a] += overall_post_all[a][i];
//            Log.d("overall", String.valueOf(sum1));
            }
        }

        double[][] average_overvall_post = new double[4][19];
        Object[][] fdmax1 = new Object[4][2];
        for (int a = 0; a < 4; a++) {
            for (int i = 0; i < 19; i++) {
                average_overvall_post[a][i] = overall_post_all[a][i] / sum1_all[a];
//            Log.d("average_overvall_post", String.valueOf(average_overvall_post[i]));
            }
            fdmax1[a] = findMax(average_overvall_post[a]);
//            predict = (int) fdmax[1];
//            proba = (double) fdmax[0];

        }
        return fdmax1;
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



