package com.luffy.mulmedia.opengl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class DragSurfaceView extends SurfaceView {
    IDrawer drawer;

    public DragSurfaceView(Context context) {
        super(context);
    }

    public DragSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDrawer(IDrawer drawer) {
        this.drawer = drawer;
    }

    private float previousX = -1;
    private float previousY = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (previousX == -1 && previousY == -1) {
                previousX = event.getX();
                previousY = event.getY();
            }
            float translateX = event.getX() - previousX;
            float translateY = event.getY() - previousY;
            drawer.translate(translateX, translateY);
            previousX = event.getX();
            previousY = event.getY();
            Log.d("DragSurfaceView", "previousX:" + previousX + ",previousY:" + previousY);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            previousX = -1;
            previousY = -1;
        }
        return true;
    }
}
