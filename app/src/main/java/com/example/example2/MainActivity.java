package com.example.example2;

import android.app.Activity;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Environment;
import java.io.FileOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;





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
     * Text fields to show the sensor values.
     */
    private TextView titleRssi,textRssi;

    Button buttonStart,buttonClear;
    RadioButton radioButton1,radioButton2,radioButton3,radioButton4,radioButton5,
            radioButton6,radioButton7,radioButton8,radioButton9,radioButton10,radioButton11,
            radioButton12,radioButton13,radioButton14,radioButton15,radioButton16,
            radioButton17,radioButton18,radioButton19;

    RadioButton radioButtonN,radioButtonE,radioButtonS,radioButtonW;


    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    int zoneID;
    int directionID;
    int remaining;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        titleRssi = (TextView) findViewById(R.id.titleRSSI);
        textRssi = (TextView) findViewById(R.id.textRSSI);



        // Create the buttons and RadioButtons
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonClear = (Button) findViewById(R.id.buttonClear);

        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
        radioButton5 = (RadioButton) findViewById(R.id.radioButton5);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton6);
        radioButton7 = (RadioButton) findViewById(R.id.radioButton7);
        radioButton8 = (RadioButton) findViewById(R.id.radioButton8);
        radioButton9 = (RadioButton) findViewById(R.id.radioButton9);
        radioButton10 = (RadioButton) findViewById(R.id.radioButton10);
        radioButton11 = (RadioButton) findViewById(R.id.radioButton11);
        radioButton12 = (RadioButton) findViewById(R.id.radioButton12);
        radioButton13 = (RadioButton) findViewById(R.id.radioButton13);
        radioButton14 = (RadioButton) findViewById(R.id.radioButton14);
        radioButton15 = (RadioButton) findViewById(R.id.radioButton15);
        radioButton16 = (RadioButton) findViewById(R.id.radioButton16);
        radioButton17 = (RadioButton) findViewById(R.id.radioButton17);
        radioButton18 = (RadioButton) findViewById(R.id.radioButton18);
        radioButton19 = (RadioButton) findViewById(R.id.radioButton19);

        radioButtonN = (RadioButton) findViewById(R.id.radioButtonN);
        radioButtonE = (RadioButton) findViewById(R.id.radioButtonE);
        radioButtonS = (RadioButton) findViewById(R.id.radioButtonS);
        radioButtonW = (RadioButton) findViewById(R.id.radioButtonW);

        final RadioGroup zones1 = (RadioGroup) findViewById(R.id.zones1);
        final RadioGroup zones2 = (RadioGroup) findViewById(R.id.zones2);
        final RadioGroup zones3 = (RadioGroup) findViewById(R.id.zones3);
        final RadioGroup zones4 = (RadioGroup) findViewById(R.id.zones4);
        final RadioGroup zones5 = (RadioGroup) findViewById(R.id.zones5);
        final RadioGroup zones6 = (RadioGroup) findViewById(R.id.zones6);


        final RadioGroup directions = (RadioGroup) findViewById(R.id.directions);


        // Create a click listener for start button.
        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (zones1.getCheckedRadioButtonId()!= -1) zoneID = zones1.getCheckedRadioButtonId();
                if (zones2.getCheckedRadioButtonId()!= -1) zoneID = zones2.getCheckedRadioButtonId();
                if (zones3.getCheckedRadioButtonId()!= -1) zoneID = zones3.getCheckedRadioButtonId();
                if (zones4.getCheckedRadioButtonId()!= -1) zoneID = zones4.getCheckedRadioButtonId();
                if (zones5.getCheckedRadioButtonId()!= -1) zoneID = zones5.getCheckedRadioButtonId();
                if (zones6.getCheckedRadioButtonId()!= -1) zoneID = zones6.getCheckedRadioButtonId();

                directionID = directions.getCheckedRadioButtonId();

                RadioButton checkedRadioButton = (RadioButton) findViewById(zoneID);
                int textZone = Integer.parseInt(checkedRadioButton.getText().toString().substring(5));

                RadioButton checkedRadioButton2 = (RadioButton) findViewById(directionID);
                String textDirection = checkedRadioButton2.getText().toString();

                textRssi.setTextColor(Color.RED);
                textRssi.setText("Recording, Selected zone: "+textZone+" Selected direction: "+textDirection);

                getWifiData(textZone,textDirection);
            }
        });

        // Create a click listener for clear button.
        buttonClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zones1.clearCheck();
                zones2.clearCheck();
                zones3.clearCheck();
                zones4.clearCheck();
                zones5.clearCheck();
                zones6.clearCheck();
                directions.clearCheck();
            }
        });


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
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // No accelerometer!
        }


        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

    }


    public void writeWifiValuesCSV(String row, int cell, String direction) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();
            String fileNameWifi1 = "wifiData_cell" + cell + direction + "_" + curTime + ".csv";

            File file = new File(dir, fileNameWifi1);
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

    public void getWifiData(final int zone,final String direction){

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            public void run() {

                int numWifiSamples = 10;

                textRssi.setText("Scan all access points:");
                // Set wifi manager.
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                // Start a wifi scan
                Map<String, ArrayList<Integer>> wifiData = new HashMap<>();
                textRssi.setText("start getting data in zone " + zone + direction + "...");
                for (int i=0; i<numWifiSamples; i++) {

                    remaining = numWifiSamples - i;

                    handler.post(new Runnable(){
                        public void run() {
                            textRssi.setText("Getting data in zone " + zone + direction + " , Remaining time: "+remaining+"s...");
                        }
                    });

                    wifiManager.startScan();
                    // Store results in a list.
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    for (ScanResult scanResult : scanResults) {
                        String key = scanResult.SSID + ","+scanResult.BSSID;
                        if (wifiData.containsKey(key)) {
                            //key exists
                            wifiData.get(key).add(scanResult.level);
                        } else {
                            //key does not exists
                            ArrayList<Integer> wifiDataList = new ArrayList<>();
                            wifiDataList.add(scanResult.level);
                            wifiData.put(key, wifiDataList);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                for (Map.Entry<String, ArrayList<Integer>> entry : wifiData.entrySet()) {
                    String row  = entry.getKey() + ",";
                    Iterator<Integer> iterator = entry.getValue().iterator();
                    while (iterator.hasNext()) {
                        row += iterator.next() + ",";
                    }
                    row  = row.substring(0,row.length()-1)+ "\n";
                    writeWifiValuesCSV(row,zone,direction);
                }



                handler.post(new Runnable(){
                    public void run() {
                        textRssi.setText("Done in Cell " + zone + " Direction "+direction);
                        textRssi.setTextColor(Color.BLACK);
                    }
                });
            }
        };
        new Thread(runnable).start();



    }
}