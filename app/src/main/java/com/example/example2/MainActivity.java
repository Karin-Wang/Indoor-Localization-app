package com.example.example2;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
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
    private TextView textRssi;



    String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    Button buttonRssi,buttonRssi1,buttonRssi2,buttonRssi3;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        textRssi = (TextView) findViewById(R.id.textRSSI);



        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        buttonRssi1 = (Button) findViewById(R.id.buttonRSSI1);
        buttonRssi2 = (Button) findViewById(R.id.buttonRSSI2);
        buttonRssi3 = (Button) findViewById(R.id.buttonRSSI3);

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

        // Create a click listener for our button.
        buttonRssi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifiData(1);
            }
        });
        buttonRssi1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifiData(2);
            }
        });
        buttonRssi2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifiData(3);
            }
        });
        buttonRssi3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifiData(4);
            }
        });

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


    public void writeWifiValuesCSV(String row, int cell) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();
            String fileNameWifi1 = "wifiData_cell" + cell + "_" + curTime + ".csv";

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

    public void getWifiData(int cell){

        int numWifiCollect = 5;
        // Set text.
        textRssi.setText("Scan all access points:");
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan
        Map<String, ArrayList<Integer>> wifiData = new HashMap<>();
        textRssi.setText("start getting data in Cell " + cell + "...");
        for (int i=0; i<numWifiCollect; i++) {
            textRssi.setText(i+"s...");
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
            writeWifiValuesCSV(row,cell);
        }
        textRssi.setText("Done in Cell " + cell);
    }
}