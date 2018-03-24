package com.example.particlefilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Daniel on 2018. 03. 23..
 */

public class MyView extends View {   // stackoverflow code

    Paint paint;
    ArrayList<Point> points = new ArrayList<>();
    Iterator<Point> iterator;



    public float x;
    public float y;
    public int radius = 10;



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
        populateArrayList();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        iterator = points.iterator();

    
        while (iterator.hasNext()) {

            Point curPoint = iterator.next();

            canvas.drawCircle(curPoint.x, curPoint.y, radius, paint);

        }
    }


    public void populateArrayList(){
        points.clear();
        points.add(new Point(1000, 400));
        points.add(new Point(200, 400));
        points.add(new Point(1600, 400));
    }

    public void updatePoints(double azimuth,Canvas canvas){

        final double angle = azimuth;

        Runnable runnable = new Runnable() {

            public void run() {

                iterator = points.iterator();

                while (iterator.hasNext()) {

                    Point curPoint = iterator.next();


                    curPoint.x += (float) (Math.cos(angle) * 5);
                    curPoint.y += (float) (Math.sin(angle) * 5);

                }
            }
        };
        new Thread(runnable).start();

        this.onDraw(canvas);

    }

    public void deletePoints(){


    }

}