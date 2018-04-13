package com.example.bayesian;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

/**
 * Created by Daniel on 2018. 03. 23..
 */

public class MyView extends View {   // stackoverflow code

    Paint paint;

    CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Particle> aliveParticles;
    CopyOnWriteArrayList<Particle> deadParticles;

    ArrayList<Rect> zones = new ArrayList<>();
    int[] zoneCounter;

    int climbcounter = 0;

    int aliveSize;


    Particle heroParticle;
    int[] mostPopulated = new int[2];
    int zoneColor;


    public float x;
    public float y;
    public int radius = 20;

    public static double INITIAL_ANGLE = 0;
    public static double INITIAL_SPEED = 71;

    public void setINITIAL_SPEED(double initial_speed) {
        INITIAL_SPEED = initial_speed;
    }

    public MyView(Context context) {
        super(context);
        x = this.getX();
        y = this.getY();
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        x = this.getX();
        y = this.getY();
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        x = this.getX();
        y = this.getY();
        init();
    }

    private void init() {
        // Load attributes
        paint = new Paint();
        paint.setColor(Color.RED);
        //populate16();
        //populateArrayList();
        populateParticles();
        populateRect();

    }

    private void populateRect() {

        zones.add(new Rect(528, 308, 751, 398)); // 1
        zones.add(new Rect(528, 403, 751, 516)); // 2
        zones.add(new Rect(751, 403, 1003, 516)); // 3
        zones.add(new Rect(1003, 403, 1178, 518)); // 4
        zones.add(new Rect(1003, 516, 1178, 780)); // 5
        zones.add(new Rect(1178, 403, 1464, 516)); // 6
        zones.add(new Rect(1178, 516, 1352, 781)); // 7
        zones.add(new Rect(1467, 486, 1650, 672)); // 8
        zones.add(new Rect(1650, 489, 1870, 607)); // 9
        zones.add(new Rect(1465, 300, 1650, 485)); // 10
        zones.add(new Rect(1465, 112, 1532, 300)); // 11
        zones.add(new Rect(1242, 44, 1532, 110)); // 12
        zones.add(new Rect(1048, 44, 1242, 180)); // 13
        zones.add(new Rect(824, 44, 1084, 180)); // 14
        zones.add(new Rect(528, 44, 824, 208)); // 15
        zones.add(new Rect(1051, 183, 1168, 400)); // 16
        zones.add(new Rect(1051, 184, 1171, 400)); // 17
        zones.add(new Rect(529, 232, 622, 400)); // 18
        zones.add(new Rect(1534, 46, 1872, 300)); // 19


    }

    private void updateZones(){

        zoneCounter = new int[19];
        Iterator<Particle> zoneiterator = particles.iterator();

        while (zoneiterator.hasNext()) {

            Particle zoneParticle = zoneiterator.next();

            for (int index = 0; index < 19; index++){

                if (zones.get(index).contains((int)zoneParticle.x, (int)zoneParticle.y)){

                    zoneCounter[index]++;
                    //break;

                }
            }
        }


       mostPopulated = getMax(zoneCounter);

        float ratio = (float)mostPopulated[1]/(float)particles.size();
        //og.d("ratio: ", String.valueOf(ratio));
        double THRESHOLD;

        if(MainActivity.getFloor()== 3) THRESHOLD  = 0.4;
        else THRESHOLD = 0.7;

        if (ratio > THRESHOLD){
            zoneColor = Color.GREEN;
        } else {
            zoneColor = Color.CYAN;
        }
    }

    public static int[] getMax(int[] inputArray){
        int maxValue = 0;
        int maxIndex = -1;

        int floor = MainActivity.getFloor();

        if (floor == 3){

            for(int i=16;i < 19;i++){
                if(inputArray[i] > maxValue){
                    maxValue = inputArray[i];
                    maxIndex = i+1;
                    //Log.d("index: ",String.valueOf(maxIndex)+" , value: "+String.valueOf(maxValue));
                }
            }

        } else {
            for(int i=0; i < 16;i++){
                if(inputArray[i] > maxValue){
                    maxValue = inputArray[i];
                    maxIndex = i+1;
                    //Log.d("index: ",String.valueOf(maxIndex)+" , value: "+String.valueOf(maxValue));
                }
            }
        }

        int[] max = {maxIndex,maxValue};
        return max;
    }

   public int checkZone(){

        if (mostPopulated[0] == 16){
            climbcounter ++;
            MainActivity.setIswalking(false);
        }
        if (mostPopulated[0] == 17){
            climbcounter ++;
            MainActivity.setIswalking(false);
        }
        if (climbcounter > 3){
            climbcounter = 0;
            MainActivity.changeFloor();
        }

        return climbcounter;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        //canvas.setBitmap(imageBitmap);
        //canvas.drawRect(zones.get(16),paint);

        Iterator<Particle> drawiterator = particles.iterator();

        while (drawiterator.hasNext()) {

            Particle curParticle = drawiterator.next();

            canvas.drawCircle(curParticle.x, curParticle.y, (float)curParticle.weight*radius, paint);

        }
        MainActivity.getFloorplan().setImageBitmap(MainActivity.getImageBitmap());

        if (mostPopulated[0] > -1)  {
            MainActivity.getPFTextView().setText("PF: "+ String.valueOf(mostPopulated[0]));
            MainActivity.getPFTextView().setTextColor(zoneColor);
        } else {
            MainActivity.getPFTextView().setText("PF: -");
            MainActivity.getPFTextView().setTextColor(Color.BLACK);
        }

    }


    public void populateArrayList(){
        particles.clear();
        //particles.add(new Particle(1, 782, 0, 0.2));
        //particles.add(new Particle(200, 400, 0, 0.2));
        //particles.add(new Particle(1000, 400, 0.5, 0.1,0));
        //particles.add(new Particle(1000, 400, 0, 0.4, 0));
        particles.add(new Particle(1642, 456, 0, 0.4, 0));
    }

        public int populate16(){

            particles.clear();

            int cntr = 0;
            int STEP = 10;
            Bitmap mask = MainActivity.getMaskBitmap();

            double[] speedvector = {-5,-2,0,2,5};
            double[] anglevector = {-0.5,-0.2,0,0.2,0.5};

            zones.add(new Rect(1040, 183, 1168, 400)); // 16

            for (int i = 1050; i < 1170;i=i+STEP){
                for (int j = 183; j < 400;j=j+STEP){

                    int color = mask.getPixel(i*3,j*3);
                    int redcomponent = (color >> 16) & 0xff;

                    if ((redcomponent) == 255) {

                        for (int speed = 0; speed < speedvector.length; speed++){
                            for (int angle = 0; angle < anglevector.length; angle++){

                                particles.add(new Particle(i, j, anglevector[angle], 0.1, speedvector[speed]));
                                //Log.d("add "," index: "+cntr+" x: "+String.valueOf(i)+" y "+String.valueOf(j)+" angleerr: "+ String.valueOf(anglevector[angle])+" speederr: "+String.valueOf(speedvector[speed]));
                                cntr++;

                            }
                        }
                    }
                }
            }

            return cntr;

        }


    public int populateParticles(){
        particles.clear();

        int cntr = 0;
        int STEP = 40;
        Bitmap mask = MainActivity.getMaskBitmap();

        double[] speedvector = {-5,-2,0,2,5};
        double[] anglevector = {-0.5,-0.2,0,0.2,0.5};

        for (int i = 0; i < mask.getWidth()/3;i=i+STEP){
            for (int j = 0; j < mask.getHeight()/3;j=j+STEP){

                int color = mask.getPixel(i*3,j*3);
                int redcomponent = (color >> 16) & 0xff;

                if ((redcomponent) == 255) {

                    for (int speed = 0; speed < speedvector.length; speed++){
                        for (int angle = 0; angle < anglevector.length; angle++){

                            particles.add(new Particle(i, j, anglevector[angle], 0.1, speedvector[speed]));
                            //Log.d("add "," index: "+cntr+" x: "+String.valueOf(i)+" y "+String.valueOf(j)+" angleerr: "+ String.valueOf(anglevector[angle])+" speederr: "+String.valueOf(speedvector[speed]));
                            cntr++;

                        }
                    }
                }
            }
        }

        return cntr;

    }

    public void resampling(CopyOnWriteArrayList<Particle> alive, CopyOnWriteArrayList<Particle> dead){

        Random random = new Random();

        aliveSize = aliveParticles.size();

        if (alive.size() == 0) {
            alive.add(heroParticle);
            dead.remove(1);
        }


        for (int index = 0; index < dead.size(); index++){

            int randomnumber = random.nextInt(alive.size());

            Particle randomParticle = alive.get(randomnumber);
            Particle currentDeadParticle = dead.get(index);

            currentDeadParticle.x = randomParticle.x;
            currentDeadParticle.y = randomParticle.y;

            alive.add(currentDeadParticle);
        }
        particles = (CopyOnWriteArrayList<Particle>) alive.clone();
    }


    public void updatePoints(double azimuth, final Canvas canvas){

        final double angle = azimuth;

        Runnable runnable = new Runnable() {

            public void run() {

                Iterator<Particle> iterator = particles.iterator();
                int cntr = 0;
                aliveParticles = new CopyOnWriteArrayList<>();
                deadParticles = new CopyOnWriteArrayList<>();

                synchronized (this) {

                    while (iterator.hasNext()) {

                        Particle curParticle = iterator.next();

                        heroParticle = curParticle; // the particle that saves us from indexOutOfBounds error


                        curParticle.x += (float) (Math.cos(angle+INITIAL_ANGLE + curParticle.angularerror) * (INITIAL_SPEED + curParticle.speederror));
                        curParticle.y += (float) (Math.sin(angle+INITIAL_ANGLE + curParticle.angularerror) * (INITIAL_SPEED + curParticle.speederror));


                        Bitmap maskBitmap = MainActivity.getMaskBitmap();

                        if(curParticle.x < 0) curParticle.x = 128;
                        if(curParticle.y < 0) curParticle.y = 60;
                        if(curParticle.x > maskBitmap.getWidth()) curParticle.x =  maskBitmap.getWidth()-65;
                        if(curParticle.y > maskBitmap.getHeight()) curParticle.y =  maskBitmap.getHeight()-65;

                        //Log.d("particle:", String.valueOf(curParticle.x));

                        int color = 0;

                        try {
                            color = maskBitmap.getPixel(((int) curParticle.x) * 3, ((int) (curParticle.y * 3)));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                        int redcomponent = (color >> 16) & 0xff;

                        //Log.d("R: ", String.valueOf(redcomponent));
                        if (redcomponent < 255) {

                            deadParticles.add(curParticle);
                        }
                        else{
                            aliveParticles.add(curParticle);
                        }
                        cntr++;
                    }
                    //Log.d("particles: ", String.valueOf(particles.size()));
                    resampling(aliveParticles, deadParticles);

                }
            }
        };

        Thread update = new Thread(runnable);
        update.start();
        try {
            update.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateZones();
        onDraw(canvas);
    }
}

class Particle{


    public float x;
    public float y;
    public double angularerror;
    public double weight;
    public double speederror;


    Particle(float x, float y, double angularerror, double weight, double speederror){
        this.x = x;
        this.y = y;
        this.angularerror = angularerror;
        this.weight = weight;
        this.speederror = speederror;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public double getAngularerror() {
        return this.angularerror;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getSpeedError() {
        return this.speederror;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setAngle(double angularerror) {
        this.angularerror = angularerror;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}