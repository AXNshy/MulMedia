package com.luffy.mulmedia.gl;

import android.opengl.EGLContext;
import android.opengl.EGLSurface;

public class EGLSurfaceHolder {
    public static final String TAG = "EGLSurfaceHolder";

    private EGLCore mEGLCore = null;

    private EGLSurface mEGLSurface = null;

    public void init(EGLContext context, int flags) {
        mEGLCore = new EGLCore();
        mEGLCore.init(context, flags);
    }

    public void createEGLSurface(Object surface, int width, int height) {
        if (surface != null) {
            mEGLSurface = mEGLCore.createWindowSurface(surface);
        } else {
            mEGLSurface = mEGLCore.createOffScreenSurface(width, height);
        }
    }

    public void swapBuffer() {
        if (mEGLSurface != null) {
            mEGLCore.swapBuffers(mEGLSurface);
        }
    }

    public void makeCurrent() {
        if (mEGLSurface != null) {
            mEGLCore.makeCurrent(mEGLSurface);
        }
    }

    public void destroyEGLSurface() {
        if (mEGLCore != null) {
            mEGLCore.destroySurface(mEGLSurface);
            mEGLSurface = null;
        }
    }

    public void release() {
        if (mEGLCore != null) {
            mEGLCore.release();
        }
    }
}
