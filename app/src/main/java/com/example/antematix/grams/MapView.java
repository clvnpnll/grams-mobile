package com.example.antematix.grams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paolo on 3/3/2017.
 */

public class MapView extends View{

    List list = new ArrayList<Checkpoint>();
    Bitmap myBitmap;
    public static int img_height;
    public static int img_width;
    int image_x,image_y, count;


    public MapView(Context context, Drawable drawable) {
        super(context);
        myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        setBackground(drawable);
        img_height = 0;
        img_width = 0;
    }

    public void setObject(Checkpoint object){
        list.add(object);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        img_height = canvas.getHeight();
        img_width = canvas.getWidth();
        count = list.size();
        Log.i("List","Count: " + count);
        for(int i=0; i<count; i++){
            Checkpoint chk = (Checkpoint) list.get(i);
            image_x = chk.getChkX() * img_width / 1000;
            image_y = chk.getChkY() * img_height / 1000;
            chk.setTapX(image_x);
            chk.setTapY(image_y);
            list.set(i,chk);
            canvas.drawBitmap(myBitmap,image_x,image_y,null);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                for(int i=0; i<count; i++) {
                    Checkpoint chk = (Checkpoint) list.get(i);
                    if (x >= chk.getTapX() && x < (chk.getTapX() + myBitmap.getWidth())
                            && y >= chk.getTapY() && y < (chk.getTapY() + myBitmap.getHeight())) {
                        Toast.makeText(this.getContext(), "Checkpoint: "+chk.getChkName(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
        }
        return false;
    }
}

