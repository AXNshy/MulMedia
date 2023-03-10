package com.luffy.mulmedia.gl;

import android.opengl.GLES20;
import android.util.Log;
import android.view.SurfaceHolder;

import com.luffy.mulmedia.gles2.IDrawer;
import com.luffy.mulmedia.utils.OpenGLUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

public class RenderThread extends Thread {
    public static final String TAG = "RenderThread";

    private RenderState mCurrentState = RenderState.NO_SURFACE;

    private EGLSurfaceHolder mEGLHolder;
    private WeakReference<SurfaceHolder> mSurfaceHolder;

    private boolean isEGLCreated = false;
    private boolean isBindEGLSurface = false;

    private Object waitLock = new Object();


    private List<IDrawer> drawers = new ArrayList<>();


    private int width;
    private int height;

    public void holdOn() {
        Log.d(TAG, "holdOn");
        synchronized (waitLock) {
            try {
                waitLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDrawers(List<IDrawer> drawers) {
        this.drawers = drawers;
    }

    public void notifyGo() {
        Log.d(TAG, "notifyGo");
        synchronized (waitLock) {
            waitLock.notify();
        }
    }


    public void onSurfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "onSurfaceCreated");
        mSurfaceHolder = new WeakReference<>(holder);
        mCurrentState = RenderState.FRESH_SURFACE;
        notifyGo();
    }


    public void onSurfaceChanged(int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        this.width = width;
        this.height = height;
        mCurrentState = RenderState.SURFACE_CHANGE;
        notifyGo();
    }


    public void onSurfaceDestroy() {
        Log.d(TAG, "onSurfaceDestroy");
        mCurrentState = RenderState.SURFACE_DESTROY;
        notifyGo();
    }

    @Override
    public void run() {
        initEGL();
        while (true) {
            switch (mCurrentState) {
                case RENDERING:
                    draw();
                    break;
                case SURFACE_DESTROY:
                    destroyEGLSurface();
                    mCurrentState = RenderState.NO_SURFACE;
                    break;
                case FRESH_SURFACE:
                    createEGLSurfaceFirst();
                    holdOn();
                    break;
                case SURFACE_CHANGE:
                    createEGLSurfaceFirst();
                    GLES20.glViewport(0, 0, width, height);
                    configWorldSize();
                    mCurrentState = RenderState.RENDERING;
                    break;
                case STOP:
                    releaseEGL();
                    return;
                default:
                    holdOn();
            }

            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        Log.d(TAG, "release");
        mCurrentState = RenderState.NO_SURFACE;
        notifyGo();
    }

    private void initEGL() {
        Log.d(TAG, "initEGL");
        mEGLHolder = new EGLSurfaceHolder();
        mEGLHolder.init(null, EGL_RECORDABLE_ANDROID);
    }

    private void createEGLSurfaceFirst() {
        Log.d(TAG, "createEGLSurfaceFirst");
        if (!isEGLCreated) {
            isEGLCreated = true;
            createEGLSurface();
            if (!isBindEGLSurface) {
                isBindEGLSurface = true;
                generateTextureId();
            }
        }
    }

    private void generateTextureId() {
        Log.d(TAG, "generateTextureId");
        int[] textureIds = OpenGLUtils.createTextureId(drawers.size());
        int i = 0;
        for (IDrawer drawer : drawers) {
            drawer.setTextureId(textureIds[i]);
            i++;
        }
    }

    private void createEGLSurface() {
        Log.d(TAG, "createEGLSurface");
        if (mSurfaceHolder != null) {
            mEGLHolder.createEGLSurface(mSurfaceHolder.get().getSurface(), -1, -1);
            mEGLHolder.makeCurrent();
        }
    }

    private void configWorldSize() {
        Log.d(TAG, "configWorldSize");
        for (IDrawer drawer : drawers) {
            drawer.setSurfaceSize(width, height);
        }
    }

    public void destroyEGLSurface() {
        Log.d(TAG, "destroyEGLSurface");
        mEGLHolder.destroyEGLSurface();
        isBindEGLSurface = false;
    }

    public void releaseEGL() {
        Log.d(TAG, "releaseEGL");
        mEGLHolder.release();
    }

    private void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        for (IDrawer drawer : drawers) {
            drawer.draw();
        }
        mEGLHolder.swapBuffer();
    }
}
