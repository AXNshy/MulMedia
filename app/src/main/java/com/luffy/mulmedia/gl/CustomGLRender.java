package com.luffy.mulmedia.gl;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


import com.luffy.mulmedia.gles2.IDrawer;

import java.util.List;

public class CustomGLRender implements SurfaceHolder.Callback {

    private RenderThread mThread = new RenderThread();

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    public CustomGLRender(List<IDrawer> drawers) {
        mThread.setDrawers(drawers);
        mThread.start();
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
        surfaceView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mThread.release();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        mThread.onSurfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mThread.onSurfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.onSurfaceDestroy();
    }

    public SurfaceHolder getHolder() {
        return surfaceHolder;
    }
}
