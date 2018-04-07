package com.example.particlefilter;

import android.app.Activity;
import android.content.res.Resources;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import java.util.Timer;
import java.util.List;
import java.util.Arrays;



/**
 * Smart Phone Sensing Example 2. Working with sensors.
 */
public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private String[] AP_filter1 = {"TUvisitor","tudelft-dastud","eduroam"};
    private List<String> AP_filter = Arrays.asList(AP_filter1);

    private Sensor accelerometer;
    private Sensor linearaccelerometer;
    private Sensor magnetometer;
    private Map<String, float[][]> radioMap1;



    private WifiManager wifiManager;


    private TextView textstep,textaz,textbay;
    Button buttonAccRecord;
    private boolean isRecord = false;
    private double accmagnitude;
    private long timestamp;
    String fileNameAcc;

    Button buttonToggleFloor, buttonBayes;
    public static int floor = 3;



    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    public double azimuth;


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
        textzone = (TextView) findViewById(R.id.textZone);

        // create buttons
        //buttonAccRecord = (Button) findViewById(R.id.recordAcc);
        fileNameAcc = "accData_" + curTime + ".csv";

        buttonReset = (Button) findViewById(R.id.reset);
        buttonToggleFloor = (Button) findViewById(R.id.toggleFloor);
        buttonBayes = (Button) findViewById(R.id.bayes);


        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // No accelerometer!
        }

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

        // if the default magnetometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            // set accelerometer
            magnetometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, magnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);


        } else {
            // No accelerometer!
        }




        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Create a click listener for acc recorder button.
        /*buttonAccRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileNameAcc;
                if (!isRecord) {
                    isRecord = true;
                    buttonAccRecord.setText("STOP RECORD");
                } else {
                    isRecord = false;
                    buttonAccRecord.setText("START RECORD");
                    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // time at first call
                    fileNameAcc = "accData_" + curTime + ".csv";
                    String row ="Acceleration Y, Accelearion Z, timestamp\n";   // write header to csv file
                    try {
                        File sdCard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdCard.getAbsolutePath() + "/localization");
                        dir.mkdir();
                        File file = new File(dir, fileNameAcc);
                        FileOutputStream f = new FileOutputStream(file,true);
                        f.write(row.getBytes());
                        f.flush();
                        f.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/

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

        buttonBayes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BufferedReader reader = null;
                InputStream rawRes = getResources().openRawResource(R.raw.radio_map1);
                try {
                    reader = new BufferedReader(new InputStreamReader(rawRes, "UTF8"));//换成你的文件名
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Bayesian bayes1 = new Bayesian();

                bayes1.chooseRadioMap(azimuth); // this is new

                
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
                textbay.setText("Guess: "+String.valueOf(guess));
            }
        });

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
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, linearaccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
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


        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);

            azimuth = updateOrientationAngles(); // when new acc data arrives, update angles

        }
        else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }


        else if (event.sensor == linearaccelerometer) {

            accmagnitude = Math.sqrt(event.values[0]*event.values[0]+event.values[2]*event.values[2]); // phone in horizontal pos, only z and y matters
            timestamp = event.timestamp;
            if(isRecord)writeAccValuesCSV(accmagnitude,timestamp);
            motionSensor(accmagnitude);
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


    public double updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        Runnable runnable = new Runnable() {

            public void run() {

                SensorManager.getRotationMatrix(mRotationMatrix, null,
                        mAccelerometerReading, mMagnetometerReading);

                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

            }
        };
        new Thread(runnable).start();


        textaz.setText("Azimuth: "+mOrientationAngles[0]);

        return mOrientationAngles[0];

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

        maskBitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan_mask_3_v6 ,options);
        maskBitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan_mask_4_v6 ,options);

        maskBitmap = maskBitmap3;



        canvas = new Canvas(imageBitmap);

        Paint p = new Paint();
        p.setColor(Color.RED);


        floorplan.setBackgroundResource(R.drawable.floorplan_final_3_v3);

        myview = new MyView(getApplicationContext());

        myview.onDraw(canvas);

        floorplan.setImageBitmap(imageBitmap);


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
            floorplan.setBackgroundResource(R.drawable.floorplan_final_4_v3);
            //floorplan.setImageResource(R.drawable.floorplan_final_4_v2);
            floorplan.setImageBitmap(imageBitmap);

        } else {

            floor = 3;
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



    public Map<String, Integer> getWifiData(Map<String, float[][]> radioMap){
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

}