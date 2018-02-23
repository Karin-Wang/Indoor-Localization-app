package com.example.example2;

import android.app.Activity;
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
    private TextView currentX, currentY, currentZ, titleAcc, textRssi;

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



    Button buttonRssi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the text views.
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        titleAcc = (TextView) findViewById(R.id.titleAcc);
        textRssi = (TextView) findViewById(R.id.textRSSI);


        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);

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
                // get the wifi info.
                wifiInfo = wifiManager.getConnectionInfo();
                // update the text.
                textRssi.setText("\n\tSSID = " + wifiInfo.getSSID()
                        + "\n\tRSSI = " + wifiInfo.getRssi()
                        + "\n\tLocal Time = " + System.currentTimeMillis());

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


        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");



        // get the the x,y,z values of the accelerometer, smoothing the values by averaging
        aX = (aX +event.values[0])/2;
        aY = (aY +event.values[1])/2;
        aZ = (aZ +event.values[2])/2;


        // display the current x,y,z accelerometer values
        currentX.setText(Double.toString(acc_min));
        currentY.setText(Float.toString(aY));
        currentZ.setText(Double.toString(acc_max));

        writeAccValuesCSV(aY);

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
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public void writeAccValuesCSV(double aY) {

        try {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/localization");
            dir.mkdir();

            File file = new File(dir, "output.csv");
            FileOutputStream f = new FileOutputStream(file,true);

            String row = "AccelY:"+aY+',';

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
}