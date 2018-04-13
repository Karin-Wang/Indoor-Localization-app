package com.example.bayesian;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaActionSound;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Arrays;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.lang.Thread;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Collections;
import java.util.Set;




/**
 * Smart Phone Sensing Example 2. Working with sensors.
 */
public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private String[] AP_filter1 = {"TUvisitor","tudelft-dastud","eduroam"};
    private List<String> AP_filter = Arrays.asList(AP_filter1);
//    private int floorTestsNum = 0;


    private Sensor linearaccelerometer;
    private Sensor gamerotation;
    private Map<String, float[][]> radioMap1;
    public String direction;
    public int guess;



    private WifiManager wifiManager;


    private TextView textstep,textaz,textbay, textbayD, textbayO;
    Button buttonAccRecord;
    private boolean isRecord = false;
    private double accmagnitude;
    private long timestamp;
    String fileNameAcc;
    public int guessB;

    Button buttonToggleFloor, buttonBayes, buttonBayesWithDirection, buttonO;
    public static int floor = 4;



    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


    public double azimuth;


    public int bayesianSize = 3;
    public int[] bayesians = new int[bayesianSize];
    public int bayesiansamplecounter = 0;
    public int MidGuess;

    private int samplecounter = 0;
    private double sum = 0;

    static boolean iswalking = false;

    public static Bitmap imageBitmap;
    public static Bitmap maskBitmap,maskBitmap3,maskBitmap4;
    Canvas canvas;
    Button buttonReset;

    private double[] accarray = new double[5];

    public static ImageView floorplan;
    MyView myview;
    public static TextView textzone;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        textstep = (TextView) findViewById(R.id.textSTEPCOUNT);
        textaz = (TextView) findViewById(R.id.textROTAZ);
        textbay = (TextView) findViewById(R.id.textBAYESIAN);
        textbayD = (TextView) findViewById(R.id.textBayesD);
        textzone = (TextView) findViewById(R.id.textZone);
        textbayO = (TextView) findViewById(R.id.textoptbayes);

        // create buttons
        //buttonAccRecord = (Button) findViewById(R.id.recordAcc);
        fileNameAcc = "accData_" + curTime + ".csv";

        buttonReset = (Button) findViewById(R.id.reset);
        buttonToggleFloor = (Button) findViewById(R.id.toggleFloor);
        buttonBayes = (Button) findViewById(R.id.bayes);
        buttonBayesWithDirection = (Button) findViewById(R.id.Bayesd);
        buttonO = (Button) findViewById(R.id.optbayes);


        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            // set accelerometer
            linearaccelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, linearaccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // set accelerometer
            linearaccelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, linearaccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {
            // set accelerometer
            gamerotation= sensorManager
                    .getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, gamerotation,
                    SensorManager.SENSOR_DELAY_GAME);


        } else {
            // No accelerometer!
        }


        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        buttonReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetInitialBelief();
            }
        });

        buttonToggleFloor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFloor();
            }
        });


        buttonBayesWithDirection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler bayesHandler = new Handler();
                Runnable bayesrun = new Runnable() {

                    public void run() {

                        direction = getDirection(azimuth); // this is new
                        BufferedReader reader = null;
                        InputStream rawRes;
                        if (direction == "North") {
                            rawRes = getResources().openRawResource(R.raw.radio_map_north_new1);
                        } else if (direction == "South") {
                            rawRes = getResources().openRawResource(R.raw.radio_map_south_new1);
                        } else if (direction == "West") {
                            rawRes = getResources().openRawResource(R.raw.radio_map_west_new1);
                        } else {
                            rawRes = getResources().openRawResource(R.raw.radio_map_east_new1);
                        }
                        try {
                            reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Bayesian bayes1 = new Bayesian();

                        bayes1.getRadioMap(reader);
                        bayes1.initialize();
                        radioMap1 = bayes1.radioMap;
                        int iter = 1;
                        guess = -1;
                        double proba = 0.0;
                        Object[] temp;
                        Map<String, Integer> wif_data;
                        Map<Integer, Integer> bayesCounter = new HashMap<>();
                        for (int i = 1; i<20; i++){
                            bayesCounter.put(i,0);
                        }
//                        int bayesCounter = 0;
                        while (proba <= 0.9) {
                            wif_data = getWifiData(radioMap1);
                            temp = bayes1.bayes(wif_data);
                            guess = (int) temp[1];
                            int t = bayesCounter.get(guess) + 1;
                            bayesCounter.put(guess, t);
//                            MidGuess = bayes1.predict;
//                            final int printguess = guess;
                            proba = (double) temp[0];
//                            final double printproba = proba;
                            Map.Entry<Integer,Integer> maxEntry = null;
                            for (Map.Entry<Integer, Integer> entry : bayesCounter.entrySet())
                            {
                                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                                {
                                    maxEntry = entry;
                                }
                            }
                            if (iter >= 25) {
                                break;
                            } else if (iter >= 14 && proba <= 0.4) {
                                iter = 1;
                                bayes1.initialize();
                                proba = 0.0;

                            }
                            else if(guess == 19 && iter<=2){
                                guess = 19;
                                break;
                            }
                            else if(maxEntry.getValue()>=6){
                                guess = maxEntry.getKey();
                                break;
                            }
                            else if(maxEntry.getValue()<2 && iter >= 10) {
                                iter = 1;
                                bayes1.initialize();
                                proba = 0.0;
                            }
                            else {
                                iter++;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();

                            }
                            Log.d("guess", String.valueOf(guess));
                            Log.d("proba", String.valueOf(proba));

                        }

                        Log.d("iter", String.valueOf(iter));
                        bayesHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                final int printguess = guess;
                                final String printDirection = direction;
                                final int printMidPredict = MidGuess;
                                textbayD.setText("G: " + String.valueOf(printguess) + "-D:" + printDirection);
                                textbayD.setTextColor(Color.BLACK);
                            }
                        });
                    }
                };
                textbayD.setText("start");
                textbayD.setTextColor(Color.RED);
                Thread run1 = new Thread(bayesrun);
                run1.start();
            }
        });

        buttonO.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("logclick","run");
                final Handler bayesHandler2 = new Handler();
                Runnable bayesrun2 = new Runnable() {
                    public void run() {
                        Log.d("run","run");

//                        direction = getDirection(azimuth); // this is new
                        BufferedReader reader = null;
                        InputStream rawRes;
                        rawRes = getResources().openRawResource(R.raw.radio_map_general);
                        try {
                            reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Bayesian bayes1 = new Bayesian();

                        bayes1.getRadioMap(reader);

                        radioMap1 = bayes1.radioMap;
                        int iter = 1;
                        guess = -1;
                        double proba = 0.0;
                        Object[] temp;
                        Map<String, Integer> wif_data;
                        Map<Integer, Integer> bayesCounter = new HashMap<>();
                        for (int i = 1; i<20; i++){
                            bayesCounter.put(i,0);
                        }
//                         cell: 1,2,3,15
                        String[] acell1_2_3_15 = {"38:90:a5:20:e9:01",
                                "38:90:a5:9d:1a:e1",
                                "38:90:a5:9d:1a:e2",
                                "38:90:a5:9d:1b:11",
                                "38:90:a5:9d:1b:12",
                                "50:0f:80:c4:60:01",
                                "50:0f:80:c4:60:02",
                                "50:0f:80:fd:7f:b0",
                                "50:0f:80:fd:7f:b1",
                                "50:0f:80:fd:7f:b2"};
                        List<String> cell1_2_3_15 = new ArrayList<String>(Arrays.asList(acell1_2_3_15));

//                        14 13 12 16
                        String[] acell14_13_12_16 = {
                                "50:0f:80:d8:f3:70",
                                "38:90:a5:20:e9:01",
                                "50:0f:80:c4:5f:62",
                                "50:0f:80:c4:5f:61",
                                "50:0f:80:c0:ce:21",
                                "50:0f:80:c0:ce:20",
                                "50:0f:80:c0:ce:22",
                                "50:0f:80:e3:79:61",
                                "50:0f:80:d8:f3:72",
                                "50:0f:80:d8:f3:71",
                                "50:0f:80:c4:5f:60",
                                "38:90:a5:8c:93:e1"};
                        List<String> cell14_13_12_16 = new ArrayList<String>(Arrays.asList(acell14_13_12_16));

//                        4 5 6 7
                        String[] acell4_5_6_7 = {
                                "38:90:a5:37:42:d2",
                                "50:0f:80:e3:79:60",
                                "50:0f:80:e3:74:21",
                                "38:90:a5:37:40:82",
                                "50:0f:80:e3:79:61",
                                "50:0f:80:ee:6d:51",
                                "50:0f:80:e3:74:20",
                                "38:90:a5:20:e9:00",
                                "50:0f:80:e3:74:22",
                                "38:90:a5:20:e9:01",
                                "50:0f:80:fd:7f:b1",
                                "38:90:a5:37:42:d1",
                                "38:90:a5:37:40:81",
                                "50:0f:80:e3:79:62",
                                "50:0f:80:ee:6d:50",
                                "50:0f:80:fd:7f:b2",
                                "38:90:a5:20:e9:02",
                                "38:90:a5:37:42:d0",
                                "50:0f:80:ee:6d:52"};
                        List<String> cell4_5_6_7 = new ArrayList<String>(Arrays.asList(acell4_5_6_7));

                        String[] acell10_8_9_11 = {
                                "38:90:a5:37:42:d2",
                                "50:0f:80:e3:79:60",
                                "38:90:a5:20:e9:01",
                                "38:90:a5:37:42:d1",
                                "38:90:a5:37:40:81",
                                "50:0f:80:e3:79:62",
                                "38:90:a5:37:40:82",
                                "50:0f:80:e3:79:61",
                                "38:90:a5:37:40:80",
                                "38:90:a5:20:e9:00",
                                "38:90:a5:20:e9:02",
                                "38:90:a5:37:42:d0"};
                        List<String> cell10_8_9_11 = new ArrayList<String>(Arrays.asList(acell10_8_9_11));

                        String[] acell17_18_19 = {
                                "50:0f:80:fd:85:01",
//                                "38:90:a5:06:ab:61",
                                "38:90:a5:20:e9:01",
//                                "50:0f:80:fd:7f:50",
                                "50:0f:80:fd:85:00",
//                                "38:90:a5:06:ab:62",
                                "50:0f:80:fd:7f:52",
//                                "38:90:a5:91:74:11",
                                "50:0f:80:fd:7f:51",
//                                "38:90:a5:20:e9:02",
                                "38:90:a5:20:e9:00",
//                                "50:0f:80:fd:85:02"
                        };
                        List<String> cell17_18_19 = new ArrayList<>(Arrays.asList(acell17_18_19));
//                        new ArrayList<>(Arrays.asList(array))

                        int[] whatcell = new int[7];
                        int zone = -1;
                        for (int i = 0; i<11; i++) {
                            wif_data = getWifiData(radioMap1);
                            while(wif_data.keySet().size()<=1){
                                wif_data = getWifiData(radioMap1);
                            }
                            List<String> keys = new ArrayList<String>(wif_data.keySet());
                            Log.d("keys", String.valueOf(keys));
//                            Log.d("cell", String.valueOf(cell1_2_3));
                            whatcell[0] += getIntersection(keys, cell1_2_3_15);
                            whatcell[1] += getIntersection(keys, cell14_13_12_16);
                            whatcell[2] += getIntersection(keys, cell4_5_6_7);
                            whatcell[3] += getIntersection(keys, cell10_8_9_11);
                            whatcell[4] += getIntersection(keys, cell17_18_19);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();

                            }
                        }
                        Log.d("zone0", String.valueOf(whatcell[0]));
                        Log.d("zone1", String.valueOf(whatcell[1]));
                        Log.d("zone2", String.valueOf(whatcell[2]));
                        Log.d("zone3", String.valueOf(whatcell[3]));
                        Log.d("zone4", String.valueOf(whatcell[4]));
//                        Log.d("zone5", String.valueOf(whatcell[5]));
//                        Log.d("zone6", String.valueOf(whatcell[6]));
                        List whatcellismax = Arrays.asList(ArrayUtils.toObject(whatcell));
                        Log.d("max", String.valueOf(Collections.max(whatcellismax)));
                        zone = whatcellismax.indexOf(Collections.max(whatcellismax));

//                        }
                        int[] myCell = null;
                        switch (zone){
                            case 0:
                                myCell = new int[]{1,2,3,15};
                                break;
                            case 1:
                                myCell = new int[]{14,13,12,16};
                                break;
                            case 2:
                                myCell = new int[]{4,5,6,7};
                                break;
                            case 3:
                                myCell = new int[]{10,8,9,11};
                                break;
                            case 4:
                                myCell = new int[]{17, 18, 19};
                                break;
                        }
                        Log.d("Zone", String.valueOf(zone));
                        if (zone != 4) {
                            bayes1.initialize_opt1();
                        }else{
                            bayes1.initialize_opt();
                        }
                        while (proba <= 0.8) {
                            wif_data = getWifiData(radioMap1);
                            while(wif_data.keySet().size()<=1){
                                wif_data = getWifiData(radioMap1);
                            }
                            if (zone != 4) {
                                temp = bayes1.bayes_optimize1(wif_data, myCell);
                                guess = myCell[(int) temp[1] - 1];
                            }
                            else{
                                temp = bayes1.bayes_optimize(wif_data, myCell);
                                guess = myCell[(int) temp[1] - 1];
                            }
                            int t = bayesCounter.get(guess) + 1;
                            bayesCounter.put(guess, t);
//                            MidGuess = bayes1.predict;
//                            final int printguess = guess;
                            proba = (double) temp[0];
//                            final double printproba = proba;
                            Map.Entry<Integer,Integer> maxEntry = null;
                            for (Map.Entry<Integer, Integer> entry : bayesCounter.entrySet())
                            {
                                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                                {
                                    maxEntry = entry;
                                }
                            }
                            if (iter >= 30) {
                                break;
                            } else if (iter >= 14 && proba <= 0.4) {
                                iter = 1;
                                bayes1.initialize_opt();
                                proba = 0.0;

                            }
                            else if(guess == 19 && iter<=2){
                                guess = 19;
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            else if(maxEntry.getValue()>=6){
                                guess = maxEntry.getKey();
                                break;
                            }
                            else if(maxEntry.getValue()<2 && iter >= 10) {
                                iter = 1;
                                bayes1.initialize_opt();
                                proba = 0.0;
                            }
                            else {
                                iter++;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();

                            }
                            Log.d("guess", String.valueOf(guess));
                            Log.d("proba", String.valueOf(proba));

                        }

                        Log.d("iter", String.valueOf(iter));
                        bayesHandler2.post(new Runnable() {
                            @Override
                            public void run() {
                                final int printguess1 = guess;
                                textbayO.setText("G: " + String.valueOf(printguess1));
                                textbayO.setTextColor(Color.BLACK);
                            }
                        });
                    }
                };
                textbayO.setText("start_general");
                textbayO.setTextColor(Color.RED);
                Thread run3 = new Thread(bayesrun2);
                run3.start();
            }
        });




//        buttonO.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("logclick","run");
//                final Handler bayesHandler2 = new Handler();
//                Runnable bayesrun2 = new Runnable() {
//                    public void run() {
//                        Log.d("run","run");
//
////                        direction = getDirection(azimuth); // this is new
//                        BufferedReader reader = null;
//                        InputStream rawRes;
//                        rawRes = getResources().openRawResource(R.raw.radio_map_general);
//                        try {
//                            reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                        Bayesian bayes1 = new Bayesian();
//
//                        bayes1.getRadioMap(reader);
//                        bayes1.initialize_opt();
//                        radioMap1 = bayes1.radioMap;
//                        int iter = 1;
//                        guess = -1;
//                        double proba = 0.0;
//                        Object[] temp;
//                        Map<String, Integer> wif_data;
//                        Map<Integer, Integer> bayesCounter = new HashMap<>();
//                        for (int i = 1; i<20; i++){
//                            bayesCounter.put(i,0);
//                        }
//                        String[] acell1_2_3 = {"38:90:a5:20:f0:f1",
//                        "38:90:a5:37:42:d2",
//                                "50:0f:80:c4:60:02",
//                                "50:0f:80:c4:60:01",
//                                "38:90:a5:9d:1b:11",
//                                "38:90:a5:20:f0:f2",
//                                "50:0f:80:fd:7f:b0",
//                                "38:90:a5:20:e9:01",
//                                "50:0f:80:fd:7f:b1",
//                                "38:90:a5:37:42:d1",
//                                "38:90:a5:9d:1a:e2",
//                                "38:90:a5:20:f0:f0",
//                                "38:90:a5:9d:1a:e0",
//                                "38:90:a5:9d:1a:e1",
//                                "38:90:a5:9d:1b:10",
//                                "50:0f:80:fd:7f:b2",
//                                "38:90:a5:20:e9:02",
//                                "38:90:a5:20:e9:00",
//                                "38:90:a5:9d:1b:12"};
//                        List<String> cell1_2_3 = new ArrayList<String>(Arrays.asList(acell1_2_3));
//
//                        String[] acell4_16_5 = {"38:90:a5:37:42:d2",
//                                "50:0f:80:e3:79:60",
//                                "38:90:a5:20:e9:01",
//                                "38:90:a5:37:42:d1",
//                                "50:0f:80:c0:ce:21",
//                                "50:0f:80:e3:79:62",
//                                "50:0f:80:c0:ce:20",
//                                "50:0f:80:c0:ce:22",
//                                "50:0f:80:e3:79:61",
//                                "38:90:a5:20:e9:00",
//                                "38:90:a5:20:e9:02",
//                                "38:90:a5:37:42:d0"};
//                        List<String> cell4_16_5 = new ArrayList<String>(Arrays.asList(acell4_16_5));
//
//                        String[] acell6_7_10 = {"38:90:a5:37:42:d2",
//                                "50:0f:80:e3:79:60",
//                                "50:0f:80:c4:5f:61",
//                                "50:0f:80:e3:74:21",
//                                "38:90:a5:37:40:82",
//                                "50:0f:80:e3:79:61",
//                                "50:0f:80:e3:74:20",
//                                "38:90:a5:20:e9:00",
//                                "38:90:a5:20:e9:01",
//                                "50:0f:80:fd:7f:b1",
//                                "38:90:a5:37:42:d1",
//                                "38:90:a5:37:40:81",
//                                "50:0f:80:e3:79:62",
//                                "38:90:a5:37:40:80",
//                                "50:0f:80:fd:7f:51",
//                                "38:90:a5:20:e9:02",
//                                "38:90:a5:37:42:d0'"};
//                        List<String> cell6_7_10 = new ArrayList<String>(Arrays.asList(acell6_7_10));
//                        String[] acell10_8_9 = {"38:90:a5:37:42:d2",
//                                "50:0f:80:e3:79:60",
//                                "50:0f:80:e3:74:21",
//                                "38:90:a5:8c:7c:d0",
//                                "38:90:a5:37:40:82",
//                                "50:0f:80:e3:79:61",
//                                "38:90:a5:8c:7c:d2",
//                                "50:0f:80:e3:74:20",
//                                "50:0f:80:e3:73:f1",
//                                "38:90:a5:20:e9:00",
//                                "38:90:a5:20:e9:01",
//                                "38:90:a5:37:42:d1",
//                                "38:90:a5:8c:7c:d1",
//                                "38:90:a5:37:40:81",
//                                "50:0f:80:e3:79:62",
//                                "38:90:a5:37:40:80",
//                                "38:90:a5:20:e9:02",
//                                "38:90:a5:37:42:d0"};
//                        List<String> cell10_8_9 = new ArrayList<String>(Arrays.asList(acell10_8_9));
//
//                        String[] acell13_11_12 = {"50:0f:80:e3:79:60",
//                                "38:90:a5:00:09:41",
//                                "50:0f:80:c4:5f:61",
//                                "50:0f:80:c0:ce:21",
//                                "50:0f:80:c0:ce:20",
//                                "50:0f:80:fd:7f:52",
//                                "50:0f:80:e3:79:61",
//                                "50:0f:80:c0:ce:22",
//                                "38:90:a5:8c:93:e0",
//                                "50:0f:80:fd:7f:50",
//                                "38:90:a5:20:e9:01",
//                                "50:0f:80:c4:5f:62",
//                                "38:90:a5:91:7f:81",
//                                "50:0f:80:e3:79:62",
//                                "38:90:a5:8c:93:e2",
//                                "50:0f:80:fd:7f:51",
//                                "50:0f:80:c4:5f:60",
//                                "38:90:a5:8c:93:e1"};
//                        List<String> cell13_11_12 = new ArrayList<String>(Arrays.asList(acell13_11_12));
//
//                        String[] acell13_14_15 = {"50:0f:80:d8:f3:70",
//                                "50:0f:80:c4:60:02",
//                                "50:0f:80:c4:60:01",
//                                "50:0f:80:fe:3f:81",
//                                "50:0f:80:c0:ce:21",
//                                "50:0f:80:c0:ce:20",
//                                "50:0f:80:c0:ce:22",
//                                "50:0f:80:d8:f3:72",
//                                "50:0f:80:c4:5f:91",
//                                "50:0f:80:c4:60:00",
//                                "50:0f:80:fd:7f:b0",
//                                "50:0f:80:d8:f3:71",
//                                "38:90:a5:20:e9:01",
//                                "50:0f:80:d8:ee:01",
//                                "50:0f:80:fd:7f:b1",
//                                "38:90:a5:91:7f:81",
//                                "38:90:a5:37:46:92",
//                                "50:0f:80:fd:7f:b2",
//                                "50:0f:80:d8:f4:21",
//                                "38:90:a5:8c:93:e1"};
//                        List<String> cell13_14_15 = new ArrayList<String>(Arrays.asList(acell13_14_15));
//
//                        String[] acell17_18_19 = {
//                                "50:0f:80:e3:79:60",
//                                "50:0f:80:fd:85:01",
//                                "50:0f:80:fd:7f:50",
//                                "38:90:a5:06:ab:61",
//                                "50:0f:80:fd:85:00",
//                                "50:0f:80:e3:74:21",
//                                "38:90:a5:06:ab:62",
//                                "50:0f:80:e3:79:62",
//                                "50:0f:80:fd:7f:52",
//                                "50:0f:80:e3:79:61",
//                                "38:90:a5:91:74:11",
//                                "50:0f:80:fd:7f:51",
//                                "50:0f:80:fd:85:02",
//                                "50:0f:80:e3:74:22"};
//                        List<String> cell17_18_19 = new ArrayList<>(Arrays.asList(acell17_18_19));
////                        new ArrayList<>(Arrays.asList(array))
//
//                        int[] whatcell = new int[7];
//                        int zone = -1;
//                        for (int i = 0; i<11; i++) {
//                            wif_data = getWifiData(radioMap1);
//                            while(wif_data.keySet().size()<=2){
//                                wif_data = getWifiData(radioMap1);
//                            }
//                            List<String> keys = new ArrayList<String>(wif_data.keySet());
//                            Log.d("keys", String.valueOf(keys));
//                            Log.d("cell", String.valueOf(cell1_2_3));
//                            whatcell[0] += getIntersection(keys, cell1_2_3);
//                            whatcell[1] += getIntersection(keys, cell4_16_5);
//                            whatcell[2] += getIntersection(keys, cell6_7_10);
//                            whatcell[3] += getIntersection(keys, cell10_8_9);
//                            whatcell[4] += getIntersection(keys, cell13_11_12);
//                            whatcell[5] += getIntersection(keys, cell13_14_15);
////                            whatcell[6] += getIntersection(keys, cell17_18_19);
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//
//                            }
//                        }
//                            Log.d("zone0", String.valueOf(whatcell[0]));
//                        Log.d("zone1", String.valueOf(whatcell[1]));
//                        Log.d("zone2", String.valueOf(whatcell[2]));
//                        Log.d("zone3", String.valueOf(whatcell[3]));
//                        Log.d("zone4", String.valueOf(whatcell[4]));
//                        Log.d("zone5", String.valueOf(whatcell[5]));
//                        Log.d("zone6", String.valueOf(whatcell[6]));
//                            List whatcellismax = Arrays.asList(ArrayUtils.toObject(whatcell));
//                            Log.d("max", String.valueOf(Collections.max(whatcellismax)));
//                            zone = whatcellismax.indexOf(Collections.max(whatcellismax));
//
////                        }
//                        int[] myCell = null;
//                        switch (zone){
//                            case 0:
//                                myCell = new int[]{1, 2, 3};
//                                break;
//                            case 1:
//                                myCell = new int[]{4, 16, 5};
//                                break;
//                            case 2:
//                                myCell = new int[]{6, 7, 10};
//                                break;
//                            case 3:
//                                myCell = new int[]{10, 8, 9};
//                                break;
//                            case 4:
//                                myCell = new int[]{13, 11, 12};
//                                break;
//                            case 5:
//                                myCell = new int[]{13, 14, 15};
//                                break;
////                            case 6:
////                                myCell = new int[]{17, 18, 19};
////                                break;
//                        }
//                        Log.d("Zone", String.valueOf(zone));
//
//                        while (proba <= 0.8) {
//                            wif_data = getWifiData(radioMap1);
//                            while(wif_data.keySet().size()<=2){
//                                wif_data = getWifiData(radioMap1);
//                            }
//
//                            temp = bayes1.bayes_optimize(wif_data, myCell);
//                            guess = myCell[(int) temp[1]-1];
//                            int t = bayesCounter.get(guess) + 1;
//                            bayesCounter.put(guess, t);
////                            MidGuess = bayes1.predict;
////                            final int printguess = guess;
//                            proba = (double) temp[0];
////                            final double printproba = proba;
//                            Map.Entry<Integer,Integer> maxEntry = null;
//                            for (Map.Entry<Integer, Integer> entry : bayesCounter.entrySet())
//                            {
//                                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
//                                {
//                                    maxEntry = entry;
//                                }
//                            }
//                            if (iter >= 30) {
//                                break;
//                            } else if (iter >= 14 && proba <= 0.4) {
//                                iter = 1;
//                                bayes1.initialize_opt();
//                                proba = 0.0;
//
//                            }
//                            else if(guess == 19 && iter<=2){
//                                guess = 19;
//                                try {
//                                    Thread.sleep(1500);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                break;
//                            }
//                            else if(maxEntry.getValue()>=6){
//                                guess = maxEntry.getKey();
//                                break;
//                            }
//                            else if(maxEntry.getValue()<2 && iter >= 10) {
//                                iter = 1;
//                                bayes1.initialize_opt();
//                                proba = 0.0;
//                            }
//                            else {
//                                iter++;
//                            }
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//
//                            }
//                            Log.d("guess", String.valueOf(guess));
//                            Log.d("proba", String.valueOf(proba));
//
//                        }
//
//                        Log.d("iter", String.valueOf(iter));
//                        bayesHandler2.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                final int printguess1 = guess;
//                                textbayO.setText("G: " + String.valueOf(printguess1));
//                                textbayO.setTextColor(Color.BLACK);
//                            }
//                        });
//                    }
//                };
//                textbayO.setText("start_general");
//                textbayO.setTextColor(Color.RED);
//                Thread run3 = new Thread(bayesrun2);
//                run3.start();
//            }
//        });

        buttonBayes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("logclick","run");
                final Handler bayesHandler1 = new Handler();
                Runnable bayesrun1 = new Runnable() {
                    public void run() {
                        Log.d("run","run");

//                        direction = getDirection(azimuth); // this is new
                        BufferedReader reader = null;
                        InputStream rawRes;
                        rawRes = getResources().openRawResource(R.raw.radio_map_general);
                        try {
                            reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Bayesian bayes1 = new Bayesian();

                        bayes1.getRadioMap(reader);
                        bayes1.initialize();
                        radioMap1 = bayes1.radioMap;
                        int iter = 1;
                        guess = -1;
                        double proba = 0.0;
                        Object[] temp;
                        Map<String, Integer> wif_data;
                        Map<Integer, Integer> bayesCounter = new HashMap<>();
                        for (int i = 1; i<20; i++){
                            bayesCounter.put(i,0);
                        }
//                        int bayesCounter = 0;
                        while (proba <= 0.8) {
                            wif_data = getWifiData(radioMap1);
                            while(wif_data.keySet().size()<=1){
                                wif_data = getWifiData(radioMap1);
                            }
                            temp = bayes1.bayes(wif_data);
                            guess = (int) temp[1];
                            int t = bayesCounter.get(guess) + 1;
                            bayesCounter.put(guess, t);
//                            MidGuess = bayes1.predict;
//                            final int printguess = guess;
                            proba = (double) temp[0];
//                            final double printproba = proba;
                            Map.Entry<Integer,Integer> maxEntry = null;
                            for (Map.Entry<Integer, Integer> entry : bayesCounter.entrySet())
                            {
                                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                                {
                                    maxEntry = entry;
                                }
                            }
                            if (iter >= 30) {
                                break;
                            } else if (iter >= 14 && proba <= 0.4) {
                                iter = 1;
                                bayes1.initialize();
                                proba = 0.0;

                            }
                            else if(guess == 19 && iter<=2){
                                guess = 19;
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            else if(maxEntry.getValue()>=6){
                                guess = maxEntry.getKey();
                                break;
                            }
                            else if(maxEntry.getValue()<2 && iter >= 10) {
                                iter = 1;
                                bayes1.initialize();
                                proba = 0.0;
                            }
                            else {
                                iter++;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();

                            }
                            Log.d("guess", String.valueOf(guess));
                            Log.d("proba", String.valueOf(proba));

                        }

                        Log.d("iter", String.valueOf(iter));
                        bayesHandler1.post(new Runnable() {
                            @Override
                            public void run() {
                                final int printguess1 = guess;
                                textbay.setText("G: " + String.valueOf(printguess1));
                                textbay.setTextColor(Color.BLACK);
                            }
                        });
                    }
                };
                textbay.setText("start_general");
                textbay.setTextColor(Color.RED);
                Thread run2 = new Thread(bayesrun1);
                run2.start();
            }
        });

//
//        buttonBayes.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Handler bayesHandler1 = new Handler();
//                Runnable bayesrun1 = new Runnable() {
//
//                    public void run() {
//                        Map<String, float[][]>[] radioMap_All;
//                        BufferedReader[] reader = new BufferedReader[4];
//                        InputStream rawResN = getResources().openRawResource(R.raw.radio_map_north);
//                        InputStream rawResS = getResources().openRawResource(R.raw.radio_map_south);
//                        InputStream rawResW = getResources().openRawResource(R.raw.radio_map_west);
//                        InputStream rawResE = getResources().openRawResource(R.raw.radio_map_east);
//                        try {
//                            reader[0] = new BufferedReader(new InputStreamReader(rawResN, "UTF8"));
//                            reader[1] = new BufferedReader(new InputStreamReader(rawResS, "UTF8"));
//                            reader[2] = new BufferedReader(new InputStreamReader(rawResW, "UTF8"));
//                            reader[3] = new BufferedReader(new InputStreamReader(rawResE, "UTF8"));
//
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                        Bayesian bayes1 = new Bayesian();
//                        bayes1.getAllRadioMap(reader);
//                        bayes1.initaialize_new();
//                        radioMap_All = bayes1.radioMap_All;
//                        int iter = 1;
//                        int[] guess = {-1, -1, -1, -1};
//                        double[] proba = {0.0, 0.0, 0.0, 0.0};
//                        double highest_proba = 0.0;
//                        List pb = null;
//                        Object[] temp;
//                        Map<String, Integer> wif_data;
//                        while (highest_proba <= 0.75) {
//                            wif_data = getWifiDataNew(radioMap_All);
//                            temp = bayes1.bayes_new(wif_data);
//                            for (int x = 0; x < 4; x++) {
//                                Object[] a = (Object[]) temp[x];
//                                guess[x] = (int) a[1];
//                                proba[x] = (double) a[0];
//                                Log.d("guess", String.valueOf(guess[x]));
//                                Log.d("proba", String.valueOf(proba[x]));
//                            }
//                            Log.d("0000000", "---------------------");
//
//                            pb = Arrays.asList(ArrayUtils.toObject(proba));
//                            highest_proba = (double) Collections.max(pb);
//                            Log.d("highest_proba", String.valueOf(highest_proba));
//                            if (iter >= 20) {
//                                break;
//                            } else if (iter >= 14 && highest_proba <= 0.4) {
//                                iter = 1;
//                                bayes1.initialize();
//                                proba[0] = 0.0;
//                                proba[1] = 0.0;
//                                proba[2] = 0.0;
//                                proba[3] = 0.0;
//                                highest_proba = 0.0;
//                            } else if (guess[pb.indexOf(highest_proba)] == 19 && iter <= 2) {
//
//                                guess[pb.indexOf(highest_proba)] = 19;
//                                break;
//
//                            } else {
//                                iter++;
//                            }
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                        guessB = guess[pb.indexOf(highest_proba)];
//                        bayesHandler1.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                final int guessb1 = guessB;
////                                final int printguess = guess[pb.indexOf(highest_proba)];
////                                final String printDirection = direction;
////                                final int printMidPredict = MidGuess;
////                                textbayD.setText("G: " + String.valueOf(printguess) + "-D:" + printDirection);
////                                textbayD.setTextColor(Color.BLACK);
//
//
////                                Log.d("iter", String.valueOf(iter));
//                                textbay.setText("Guess: " + String.valueOf(guessb1));
//                                textbay.setTextColor(Color.BLACK);
//                            }
//                        });
//
//                    }
//                };
//                textbay.setText("start");
//                textbay.setTextColor(Color.RED);
//                Thread run2 = new Thread(bayesrun1);
//                run2.start();
//            }
//        });

        final Handler initHandler = new Handler();  // delay this task, prevent app from crash
        initHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initCanvas();
            }
        }, 300);  //the time is in miliseconds
    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, linearaccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gamerotation,
                SensorManager.SENSOR_DELAY_GAME);
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor == linearaccelerometer) {

            accmagnitude = Math.sqrt(event.values[0]*event.values[0]+event.values[2]*event.values[2]); // phone in horizontal pos, only z and y matters
            timestamp = event.timestamp;
            if(isRecord)writeAccValuesCSV(accmagnitude,timestamp);
            motionSensor(accmagnitude);
        }

        else if (event.sensor == gamerotation) {

            azimuth = Math.PI*-event.values[2];

            String az = String.valueOf(azimuth);
            textaz.setText(az.substring(0,4));
            //Log.d("Game rotation: ", String.valueOf(az));

        }
    }

    public void writeAccValuesCSV(double accmagnitude,long timestamp) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();

            File file = new File(dir, fileNameAcc);
            FileOutputStream f = new FileOutputStream(file,true);

            String row = String.valueOf(accmagnitude)+ " , "+timestamp+"\n";

            try {
                f.write(row.getBytes());
                f.flush();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void motionSensor(double accmagnitude) {

        int windowsize = 5;

        double deviation = 0;
        double mean;
        double WALK_LIMIT = 0.10;
        double STEP_LIMIT = 0.06;
        double CLIMB_LIMIT = 0.3;


        if(samplecounter == windowsize-1) {
            // do  standard deviation
            mean = sum/windowsize;
            for (int i=0; i<windowsize; i++) {
                deviation = Math.abs(accarray[i]-mean)/windowsize;
            }

            if (deviation > WALK_LIMIT) {
                textstep.setText("Walking");
                iswalking = true;
            }
            if (deviation < STEP_LIMIT) {
                textstep.setText("Standing");
                iswalking = false;
            }
            if (deviation > CLIMB_LIMIT) {

//                int climbcntr = myview.checkZone();
//                textstep.setText("Climbing"+climbcntr);
            }

            sum = 0;
            samplecounter = 0;


        }
        else {
            accarray[samplecounter] = accmagnitude;
            sum += accmagnitude;
            samplecounter++;

        }
    }

    public static void setIswalking(boolean walking){ iswalking = walking;}


    public void initCanvas(){   // stackoverflow code


        floorplan=(ImageView)findViewById(R.id.floorplan);

        imageBitmap = Bitmap.createBitmap(floorplan.getMeasuredWidth(), floorplan.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        imageBitmap = imageBitmap.copy(imageBitmap.getConfig(), true); // it has to be mutable to draw over the floorplan


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        maskBitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan_mask_3_v8 ,options);
        maskBitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan_mask_4_v8 ,options);

        maskBitmap = maskBitmap3;



        canvas = new Canvas(imageBitmap);

        Paint p = new Paint();
        p.setColor(Color.RED);


        floorplan.setBackgroundResource(R.drawable.floorplan_final_4_v3);

        myview = new MyView(getApplicationContext());

        myview.onDraw(canvas);

        floorplan.setImageBitmap(imageBitmap);


//        final Handler continuousBayesian = new Handler();  // set a handler for updating the points in every "delaymilis" time period, code from stackoverflow
//        continuousBayesian.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                BufferedReader reader = null;
//                InputStream rawRes = getResources().openRawResource(R.raw.radio_map_general);
//                try {
//                    reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                Bayesian bayes1 = new Bayesian();
//
////                bayes1.chooseRadioMap(azimuth); // this is new
//
//
//                bayes1.getRadioMap(reader);
//                bayes1.initialize();
//                radioMap1 = bayes1.radioMap;
//                int iter = 1;
//                int guess = -1;
//                double proba = 0.0;
//                Object[] temp;
//                Map<String, Integer> wif_data ;
//                while(proba <= 0.9){
//                    wif_data = getWifiData(radioMap1);
//                    temp = bayes1.bayes(wif_data);
//                    guess = (int)temp[1];
//                    proba = (double)temp[0];
//                    if (iter >= 20){
//                        break;
//                    }
//                    else if(iter>=14 && proba<=0.4){
//                        iter = 1;
//                        bayes1.initialize();
//                        proba = 0.0;
//
//                    }
//                    else{
//                        iter++;
//                    }
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                //Log.d("iter", String.valueOf(iter));
////                textbay.setText("Guess: "+String.valueOf(guess));
////                textbay.setTextColor(Color.RED);
//
//                bayesians[bayesiansamplecounter] = guess;
//                //Log.d("guess", String.valueOf(guess));
//                //Log.d("bayesiansamplecounter", String.valueOf(bayesiansamplecounter));
//
//                if(bayesiansamplecounter == bayesianSize-1) {
//                    // do  stuff
//
//                    int threecounter = 0;
//                    int fourconuter = 0;
//
//                    for (int i=0; i<bayesianSize; i++) {
//                        if(bayesians[i] == 17 || bayesians[i] == 19 || bayesians[i] == 19) threecounter++;
//                        else fourconuter++;
//                    }
//
//                    if(threecounter > fourconuter) {
//                        //Log.d("floor","floor4");
//                        //floor = 3;
//                        //changeFloor();
//                    }
//                    else {
//                        //Log.d("floor::","floor3");
//                        //floor = 4;
//                        //changeFloor();
//                    }
//
//                    bayesiansamplecounter = 0;
//
//
//                }
//                else {
//                    //Log.d("here","-----------");
////                    bayesians[bayesiansamplecounter] = guess;
//
//                    bayesiansamplecounter++;
//                }
//
//                continuousBayesian.postDelayed(this,200);
//            }
//        }, 0);  //the time is in miliseconds



        final Handler myHandler = new Handler();  // set a handler for updating the points in every "delaymilis" time period, code from stackoverflow
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(iswalking) {
//                    myview.updatePoints(azimuth, canvas);
                }
                myHandler.postDelayed(this,200);
            }
        }, 0);  //the time is in miliseconds


    }


    public static void changeFloor(){

        if (floor == 3){

            floor = 4;
            //myview.setINITIAL_SPEED(73);
            floorplan.setBackgroundResource(R.drawable.floorplan_final_4_v3);
            //floorplan.setImageResource(R.drawable.floorplan_final_4_v2);
            floorplan.setImageBitmap(imageBitmap);

        } else {

            floor = 3;
            //myview.setINITIAL_SPEED(80);
            floorplan.setBackgroundResource(R.drawable.floorplan_final_3_v3);
            //floorplan.setImageResource(R.drawable.floorplan_final_3_v2);
            floorplan.setImageBitmap(imageBitmap);

        }
    }

    public void resetInitialBelief() {

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        myview.populateParticles();

        myview.onDraw(canvas);

        floorplan.setImageBitmap(imageBitmap);

    }

    public static Bitmap getMaskBitmap(){

        if (floor == 3) {
            //maskBitmap.recycle();
            maskBitmap = null;
            maskBitmap = maskBitmap3;
        }

        if (floor == 4) {
            //maskBitmap.recycle();
            maskBitmap = null;
            maskBitmap = maskBitmap4;
        }
        return maskBitmap;
    }

    public static int getFloor(){

        int floornum = 3;

        if (floor == 3) {
            floornum = 3;
        }

        if (floor == 4) {
            floornum = 4;
        }
        return floornum;
    }

    public static ImageView getFloorplan(){return floorplan;}
    public static Bitmap getImageBitmap(){return imageBitmap;}
    public static TextView getPFTextView(){return textzone;}



    public Map<String, Integer> getWifiData(Map<String, float[][]> radioMap) {
        Map<String, Integer> data = new HashMap<>();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan
        Map<String, ArrayList<Integer>> wifiData = new HashMap<>();
        for (int i = 0; i<20; i++) {
            wifiManager.startScan();
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                if (AP_filter.contains(scanResult.SSID) && radioMap.containsKey(scanResult.BSSID)) {
//                if (scanResult.level> -80.0) {
                    data.put(scanResult.BSSID, scanResult.level);
//                }
                }
            }
        }
        return data;
    }

    public Map<String, Integer> getWifiDataNew(Map<String, float[][]>[] radioMap) {
        Map<String, Integer> data = new HashMap<>();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan
        Map<String, ArrayList<Integer>> wifiData = new HashMap<>();
        wifiManager.startScan();
        // Store results in a list.
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (AP_filter.contains(scanResult.SSID) && (radioMap[0].containsKey(scanResult.BSSID) ||
                    radioMap[1].containsKey(scanResult.BSSID) ||
                    radioMap[2].containsKey(scanResult.BSSID) ||
                    radioMap[3].containsKey(scanResult.BSSID)
            )) {
                data.put(scanResult.BSSID, scanResult.level);
            }
        }
        return data;
    }

    public String getDirection(double angle){


        if (angle > -Math.PI/4 && angle < Math.PI/4){
            return "North";
        } else if (angle > Math.PI/4 && angle < 3*Math.PI/4){
            return "East";
        } else if (angle < -3*Math.PI/4 && angle > -Math.PI || (angle > 3*Math.PI/4 && angle < Math.PI)){
            return "South";
        }
        else{
            return "West";
        }

    }

    public int getIntersection(List<String> a, List<String> b){
        int inter = 0;
        if (a.size() > b.size()){
            for (String s: b){
                if (a.contains(s)){
                    inter++;
                }
            }
        }
        else{
            for (String s: a){
                if (b.contains(s)){
                    inter++;
                }
            }
        }
        return inter;
    }

}