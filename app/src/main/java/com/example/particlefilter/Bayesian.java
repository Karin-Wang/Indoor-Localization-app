package com.example.particlefilter;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Created by Kaering on 3/30/18.
 */

public class Bayesian {

    private Map<String, ArrayList<Float[]>> radioMap;
    private double threshold = 0.9;
    private int maxIter = 10;

    public void getRadioMap(){
//        read csv
        radioMap = new Map<String, ArrayList<Float[]>>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object o) {
                return false;
            }

            @Override
            public boolean containsValue(Object o) {
                return false;
            }

            @Override
            public ArrayList<Float[]> get(Object o) {
                return null;
            }

            @Override
            public ArrayList<Float[]> put(String s, ArrayList<Float[]> floats) {
                return null;
            }

            @Override
            public ArrayList<Float[]> remove(Object o) {
                return null;
            }

            @Override
            public void putAll(@NonNull Map<? extends String, ? extends ArrayList<Float[]>> map) {

            }

            @Override
            public void clear() {

            }

            @NonNull
            @Override
            public Set<String> keySet() {
                return null;
            }

            @NonNull
            @Override
            public Collection<ArrayList<Float[]>> values() {
                return null;
            }

            @NonNull
            @Override
            public Set<Entry<String, ArrayList<Float[]>>> entrySet() {
                return null;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        };
        try {
            BufferedReader reader = new BufferedReader(new FileReader("a.csv"));//换成你的文件名
//            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null;
            while((line=reader.readLine())!=null){
                String ap = line;
                ArrayList<Float[]> curr = new ArrayList<Float[]>();
                for (int i=0; i<19; i++){
                    line=reader.readLine();
                    String[] item = line.split("，");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                    Float[] item1 = {Float.parseFloat(item[0]), Float.parseFloat(item[1])};
                    curr.add(item1);
                }
                radioMap.put(ap, curr);

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


    public int bayes(Map<String, Integer> test_data){
        int predict = -1;
        double[] prior = new double[19];
        for (int i = 0; i<19; i++){
            prior[i] = 1/19;
        }
        double[] post = prior.clone();
        int iteration = 0;
        int j = 0;
        double proba = 0;

        while(proba <= threshold){
            prior = post.clone();
            j = 0;
            double[] overall_post = new double[19];
            for (int i = 0; i<19; i++){
                overall_post[i] = 0;
            }
            List<Entry<String, Integer>>sorted_test_data = sortMap(test_data);
            for (int item = 0; item<30; item++){
                double[] pdf = new double[19];
                for (int i = 0; i<19; i++){
                    pdf[i] = 0;
                }
                for (int cell = 0; cell< 19; cell++){
                    String AP = sorted_test_data.get(item).getKey();
//                    pdf[cell] =;

                }

            }


        }



        return predict;
    }
}
