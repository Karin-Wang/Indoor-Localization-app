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
import android.os.CountDownTimer;

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
     * walking based on linear acceleration
     */
    private boolean walking;

    private double acc_max = 0;
    private double acc_min = 0;

    private double WALKING_ACC_LIMIT_POS = 0.7;
    private double WALKING_ACC_LIMIT_NEG = -2.0;

    private long blinderWindowSize = 200; //in miliseconds
    private long endTime;



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



        // get the the x,y,z values of the accelerometer, smoothing the values
        aX = (aX +event.values[0])/2;
        aY = (aY +event.values[1])/2;
        aZ = (aZ +event.values[2])/2;


        // display the current x,y,z accelerometer values
        currentX.setText(Double.toString(acc_min));
        currentY.setText(Float.toString(aY));
        currentZ.setText(Double.toString(acc_max));


        if (aY > WALKING_ACC_LIMIT_POS && !walking) {
            if (aY > acc_max) acc_max = aY;   // acceleration peak detection
            else {
                titleAcc.setTextColor(Color.RED);
                walking = true;
                acc_max = 0.0;
                endTime = System.currentTimeMillis() + blinderWindowSize; //won't listen to stops until it ends
            }
        }

        if (aY < WALKING_ACC_LIMIT_NEG && walking && System.currentTimeMillis() > endTime) {
            if (aY < acc_min) acc_min = aY;   // acceleration negative peak detection
            else {
                titleAcc.setTextColor(Color.BLACK);
                walking = false;
                acc_min = 0.0;
            }
        }
    }
}