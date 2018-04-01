package com.example.particlefilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Daniel on 2018. 03. 23..
 */

public class MyView extends View {   // stackoverflow code

    Paint paint;

    CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Integer> toBeRemoved = new CopyOnWriteArrayList<>();
    Iterator<Particle> iterator;

    public float x;
    public float y;
    public int radius = 20;



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
        //populateArrayList();
        populateParticles();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        //canvas.setBitmap(imageBitmap);

        iterator = particles.iterator();
    
        while (iterator.hasNext()) {

            Particle curParticle = iterator.next();

            canvas.drawCircle(curParticle.x, curParticle.y, (float)curParticle.weight*radius, paint);

        }
    }


    public void populateArrayList(){
        particles.clear();
        //particles.add(new Particle(1, 782, 0, 0.2));
        //particles.add(new Particle(200, 400, 0, 0.2));
        particles.add(new Particle(1000, 400, 0.5, 0.1));
        particles.add(new Particle(1000, 400, 0, 0.4));
        //particles.add(new Particle(1, 1, -0.5, 1));
        //particles.add(new Particle(1920, 804, -0.5, 1));
        particles.add(new Particle(1622/2, 1080/2, 0, 0.4));
    }


    public int populateParticles(){
        particles.clear();

        int cntr = 0;
        int STEP = 20;
        Bitmap mask = MainActivity.getMaskBitmap();

        for (int i = 0; i < mask.getWidth()/3;i=i+STEP){
            for (int j = 0; j < mask.getHeight()/3;j=j+STEP){

                int color = mask.getPixel(i*3,j*3);
                int redcomponent = (color >> 16) & 0xff;

                if ((redcomponent) == 255) {
                    particles.add(new Particle(i, j, 0, 0.1));
                    Log.d("add ","index: "+cntr+"x: "+String.valueOf(i)+" y "+String.valueOf(j));
                    cntr++;
                }
            }
        }

        return cntr;

    }


    public void updatePoints(double azimuth, final Canvas canvas){

        final double angle = azimuth;

        Runnable runnable = new Runnable() {

            public void run() {

                iterator = particles.iterator();
                int cntr = 0;

                synchronized (this) {

                    while (iterator.hasNext()) {

                        Particle curParticle = iterator.next();

                        curParticle.x += (float) (Math.cos(angle + curParticle.angularerror) * 5);
                        curParticle.y += (float) (Math.sin(angle + curParticle.angularerror) * 5);

                        //Log.d("X: ", String.valueOf(curParticle.x));
                        //Log.d("Y: ", String.valueOf(curParticle.y));

                        Bitmap maskBitmap = MainActivity.getMaskBitmap();
                        int color = maskBitmap.getPixel(((int) curParticle.x) * 3, ((int) (curParticle.y * 3)));


                        int redcomponent = (color >> 16) & 0xff;

                        //Log.d("R: ", String.valueOf(redcomponent));
                        if (redcomponent < 255) {

                            // TODO do stuff here
                            toBeRemoved.add(cntr);

                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
        for (Integer remove : toBeRemoved){

            particles.remove(remove);

        }


        this.onDraw(canvas);

    }


}

class Particle{


    public float x;
    public float y;
    public double angularerror;
    public double weight;

    Particle(float x, float y, double angularerror, double weight){
        this.x = x;
        this.y = y;
        this.angularerror = angularerror;
        this.weight = weight;
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