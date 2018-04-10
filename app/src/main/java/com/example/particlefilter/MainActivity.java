package com.example.particlefilter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import java.util.Map;
import java.lang.Thread;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import java.util.List;




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



    private WifiManager wifiManager;


    private TextView textstep,textaz,textbay, textbayD;
    Button buttonAccRecord;
    private boolean isRecord = false;
    private double accmagnitude;
    private long timestamp;
    String fileNameAcc;

    Button buttonToggleFloor, buttonBayes, buttonBayesWithDirection;
    public static int floor = 4;



    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());


    public double azimuth;


    public int bayesianSize = 3;
    public int[] bayesians = new int[bayesianSize];
    public int bayesiansamplecounter = 0;

    private int samplecounter = 0;
    private double sum = 0;

    boolean iswalking = false;

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

        // create buttons
        //buttonAccRecord = (Button) findViewById(R.id.recordAcc);
        fileNameAcc = "accData_" + curTime + ".csv";

        buttonReset = (Button) findViewById(R.id.reset);
        buttonToggleFloor = (Button) findViewById(R.id.toggleFloor);
        buttonBayes = (Button) findViewById(R.id.bayes);
        buttonBayesWithDirection = (Button) findViewById(R.id.Bayesd);


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
                String direction = getDirection(azimuth); // this is new
                BufferedReader reader = null;
                InputStream rawRes;
                if (direction=="North"){
                    rawRes = getResources().openRawResource(R.raw.radio_map_north);
                }
                else if (direction=="South"){
                    rawRes = getResources().openRawResource(R.raw.radio_map_south);
                }
                else if(direction=="West"){
                    rawRes = getResources().openRawResource(R.raw.radio_map_west);
                }
                else{
                    rawRes = getResources().openRawResource(R.raw.radio_map_east);
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
                int guess = -1;
                double proba = 0.0;
                Object[] temp;
                Map<String, Integer> wif_data ;
                while(proba <= 0.9){
                    wif_data = getWifiData(radioMap1);
                    temp = bayes1.bayes(wif_data);
                    guess = (int)temp[1];
                    proba = (double)temp[0];
                    if (iter >= 20){
                        break;
                    }
                    else if(iter>=14 && proba<=0.4){
                        iter = 1;
                        bayes1.initialize();
                        proba = 0.0;

                    }
                    else{
                        iter++;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("iter", String.valueOf(iter));
                textbayD.setText("G: "+String.valueOf(guess)+"-D:"+direction);
                textbayD.setTextColor(Color.BLACK);
            }
        });

//        buttonBayes.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, float[][]>[] radioMap_All;
//                BufferedReader[] reader = new BufferedReader[4];
//                InputStream rawResN = getResources().openRawResource(R.raw.radio_map_N);
//                InputStream rawResS = getResources().openRawResource(R.raw.radio_map_S);
//                InputStream rawResW = getResources().openRawResource(R.raw.radio_map_W);
//                InputStream rawResE = getResources().openRawResource(R.raw.radio_map_E);
//                try {
//                    reader[0] = new BufferedReader(new InputStreamReader(rawResN, "UTF8"));
//                    reader[1] = new BufferedReader(new InputStreamReader(rawResS, "UTF8"));
//                    reader[2] = new BufferedReader(new InputStreamReader(rawResW, "UTF8"));
//                    reader[3] = new BufferedReader(new InputStreamReader(rawResE, "UTF8"));
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                Bayesian bayes1 = new Bayesian();
//                bayes1.getAllRadioMap(reader);
//                bayes1.initialize();
//                radioMap_All = bayes1.radioMap_All;
//                int iter = 1;
//                int guess = -1;
//                double[] proba = {0.0,0.0,0.0,0.0};
//                double highest_proba = 0.0;
//                Object[] temp;
//                Map<String, Integer> wif_data ;
//                while(highest_proba <= 0.9){
//                    wif_data = getWifiDataNew(radioMap_All);
//                    temp = bayes1.bayes_new(wif_data);
//                    guess = (int)temp[1];
//                    proba = (double[])temp[0];
//                    List pb = Arrays.asList(ArrayUtils.toObject(proba));
//                    highest_proba = (double) Collections.min(pb);
//                    if (iter >= 20){
//                        break;
//                    }
//                    else if(iter>=14 && highest_proba<=0.4){
//                        iter = 1;
//                        bayes1.initialize();
//                        proba[0] = 0.0;
//                        proba[0] = 0.0;
//                        proba[0] = 0.0;
//                        proba[0] = 0.0;
//
//
//                                0.0, 0.0, 0.0, 0.0};
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
//                Log.d("iter", String.valueOf(iter));
//                textbay.setText("Guess: "+String.valueOf(guess));
//                textbay.setTextColor(Color.BLACK);
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

            sum = 0;
            samplecounter = 0;


        }
        else {
            accarray[samplecounter] = accmagnitude;
            sum += accmagnitude;
            samplecounter++;

        }
    }


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


        final Handler continuousBayesian = new Handler();  // set a handler for updating the points in every "delaymilis" time period, code from stackoverflow
        continuousBayesian.postDelayed(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                InputStream rawRes = getResources().openRawResource(R.raw.radio_map1);
                try {
                    reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Bayesian bayes1 = new Bayesian();

//                bayes1.chooseRadioMap(azimuth); // this is new


                bayes1.getRadioMap(reader);
                bayes1.initialize();
                radioMap1 = bayes1.radioMap;
                int iter = 1;
                int guess = -1;
                double proba = 0.0;
                Object[] temp;
                Map<String, Integer> wif_data ;
                while(proba <= 0.9){
                    wif_data = getWifiData(radioMap1);
                    temp = bayes1.bayes(wif_data);
                    guess = (int)temp[1];
                    proba = (double)temp[0];
                    if (iter >= 20){
                        break;
                    }
                    else if(iter>=14 && proba<=0.4){
                        iter = 1;
                        bayes1.initialize();
                        proba = 0.0;

                    }
                    else{
                        iter++;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Log.d("iter", String.valueOf(iter));
//                textbay.setText("Guess: "+String.valueOf(guess));
//                textbay.setTextColor(Color.RED);

                bayesians[bayesiansamplecounter] = guess;
                //Log.d("guess", String.valueOf(guess));
                //Log.d("bayesiansamplecounter", String.valueOf(bayesiansamplecounter));

                if(bayesiansamplecounter == bayesianSize-1) {
                    // do  stuff

                    int threecounter = 0;
                    int fourconuter = 0;

                    for (int i=0; i<bayesianSize; i++) {
                        if(bayesians[i] == 17 || bayesians[i] == 19 || bayesians[i] == 19) threecounter++;
                        else fourconuter++;
                    }

                    if(threecounter > fourconuter) {
                        //Log.d("floor","floor4");
                        //floor = 3;
                        //changeFloor();
                    }
                    else {
                        //Log.d("floor::","floor3");
                        //floor = 4;
                        //changeFloor();
                    }

                    bayesiansamplecounter = 0;


                }
                else {
                    //Log.d("here","-----------");
//                    bayesians[bayesiansamplecounter] = guess;

                    bayesiansamplecounter++;
                }

                continuousBayesian.postDelayed(this,200);
            }
        }, 0);  //the time is in miliseconds



        final Handler myHandler = new Handler();  // set a handler for updating the points in every "delaymilis" time period, code from stackoverflow
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(iswalking) {
                    myview.updatePoints(azimuth, canvas);
                }
                myHandler.postDelayed(this,200);
            }
        }, 0);  //the time is in miliseconds


    }


    public void changeFloor(){

        if (floor == 3){

            floor = 4;
            myview.setINITIAL_SPEED(73);
            floorplan.setBackgroundResource(R.drawable.floorplan_final_4_v3);
            //floorplan.setImageResource(R.drawable.floorplan_final_4_v2);
            floorplan.setImageBitmap(imageBitmap);

        } else {

            floor = 3;
            myview.setINITIAL_SPEED(80);
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
        wifiManager.startScan();
        // Store results in a list.
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (AP_filter.contains(scanResult.SSID) && radioMap.containsKey(scanResult.BSSID)) {
                data.put(scanResult.BSSID, scanResult.level);
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
        double Q1 = -1.41; // value at pi/8
        double Q2 = 0.15; // value at 3/pi*8
        double Q3 = 1.72; // value at 5*pi/8

        if (angle > -Math.PI && angle < Q1){
            return "North";
        } else if (angle > Q1 && angle < Q2){
            return "East";
        } else if (angle > Q2 && angle < Q3){
            return "South";
        }
        else{
            return "West";
        }

    }

}