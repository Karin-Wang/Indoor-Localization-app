package com.example.example2;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Environment;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;




/**
 * Smart Phone Sensing Example 2. Working with sensors.
 */
public class MainActivity extends Activity implements SensorEventListener {

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    /**
     * The accelerometer.
     */
    private Sensor accelerometer;
    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;
    /**
     * The wifi info.
     */
    private WifiInfo wifiInfo;
    /**
     * Accelerometer x value
     */
    private float aX = 0;
    /**
     * Accelerometer y value
     */
    private float aY = 0;
    /**
     * Accelerometer z value
     */
    private float aZ = 0;

    /**
     * Text fields to show the sensor values.
     */
    private TextView currentY, currentZ, titleAcc, textRssi;

    /**
     * timestamp for sensor valies
     */
    private long timestamp;

    /**
     * walking detection based on linear acceleration
     */
    private boolean walking;  // boolean for making differece between walking and not walking state

    private double acc_max = 0; // used for positive acceleration peak detection
    private double acc_min = 0; // used for negative acceleration peak detection

    private double WALKING_ACC_LIMIT_POS = 0.7; // threshold for changing states, positive acceleration
    private double WALKING_ACC_LIMIT_NEG = -2.0; // threshold for changing states, negative acceleration

    private long blinderWindowSize = 400; //in miliseconds, not listening to state changes for this tome period
    private long endTime; // to store the end time of blinder window

    String fileNameAcc; // acceleration  data filename with current time

    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String fileNameWifi = "wifiData_" + curTime + ".csv";

    Button buttonRssi;
    Button buttonAccRecord;

    private boolean isRecord = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        titleAcc = (TextView) findViewById(R.id.titleAcc);
        textRssi = (TextView) findViewById(R.id.textRSSI);


        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);

        // Create acceleration button recorder
        buttonAccRecord = (Button) findViewById(R.id.buttonAccRecord);

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);

        } else {
            // No accelerometer!
        }



        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Create a click listener for our button.
        buttonRssi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set text.
                textRssi.setText("\n\tScan all access points:");
                // Set wifi manager.
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                // Start a wifi scan.
                wifiManager.startScan();
                // Store results in a list.
                List<ScanResult> scanResults = wifiManager.getScanResults();
                // Write results to a label
                for (ScanResult scanResult : scanResults) {
                    textRssi.setText(textRssi.getText() + "\n\tBSSID = "
                            + scanResult.BSSID + "    RSSI = "
                            + scanResult.level + "dBm");
                String row = scanResult.BSSID+" , " + scanResult.level + " , " + String.valueOf(scanResult.timestamp) + "\n";
                writeWifiValuesCSV(row);
                }
            }
        });

        // Create a click listener for acc recorder button.
        buttonAccRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecord) {
                    isRecord = true;
                    buttonAccRecord.setText("STOP RECORD");
                    currentY.setTextColor(Color.RED);
                    currentZ.setTextColor(Color.RED);

                } else {
                    isRecord = false;
                    currentY.setTextColor(Color.BLACK);
                    currentZ.setTextColor(Color.BLACK);

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
        });

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

        readAccTrainCSV(); // read trained acceleration data from .csv file

    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
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


        currentY.setText("0.0");
        currentZ.setText("0.0");


        // get the the y values of the accelerometer
        aX =event.values[0];
        aY =event.values[1];
        aZ =event.values[2];

        timestamp = event.timestamp;


        // display the current x,y,z accelerometer values
        currentY.setText(Float.toString(aY));
        currentZ.setText(Float.toString(aZ));

        if(isRecord)writeAccValuesCSV(aY,aZ,timestamp);

        // signal processing based motion detection
        /*if (aY > WALKING_ACC_LIMIT_POS && !walking) {
            if (aY > acc_max) acc_max = aY;   // acceleration peak detection
            else {
                titleAcc.setTextColor(Color.RED);
                walking = true;
                acc_max = 0.0;
                endTime = System.currentTimeMillis() + blinderWindowSize; //won't listen to negative acc until time ends
            }
        }

        if (aY < WALKING_ACC_LIMIT_NEG && walking && System.currentTimeMillis() > endTime) {
            if (aY < acc_min) acc_min = aY;   // acceleration negative peak detection
            else {
                titleAcc.setTextColor(Color.BLACK);
                walking = false;
                acc_min = 0.0;
            }
        }*/
    }


    public void writeAccValuesCSV(double aY, double aZ,long timestamp) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();

            File file = new File(dir, fileNameAcc);
            FileOutputStream f = new FileOutputStream(file,true);

            String row = String.valueOf(aY)+" , "+ String.valueOf(aZ)+" , "+timestamp+"\n";

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

    public void writeWifiValuesCSV(String row) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();

            File file = new File(dir, fileNameWifi);
            FileOutputStream f = new FileOutputStream(file,true);


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

    public void accelerationKNN() {

    }

    public void readAccTrainCSV(){

        if (isExternalStorageWritable()){

            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/traindata.csv";

            try{
                FileInputStream csvInput = new FileInputStream(path);
                List accTrainData =  csvread(csvInput);
            } catch (Exception e) {
                e.printStackTrace();
            }

            


        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public List csvread(InputStream inputStream){
        List resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return resultList;
    }
}