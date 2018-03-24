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
    //private int pointsPos = 0; //Which point we will be drawing
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

        //ArrayList<Point> points = new ArrayList<>();
        //List<x> myList = new ArrayList<x>();
        //public ArrayList<x> GetList()

        Iterator<Point>iterator = points.iterator();

        while (iterator.hasNext()) {

            Point curPoint = iterator.next();

            canvas.drawCircle(curPoint.x, curPoint.y, radius, paint);

        }
    }


    public void populateArrayList(){
        points.clear();
        points.add(new Point(10, 10));
        points.add(new Point(220, 40));
    }

}