package com.luffy.mulmedia.egl;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

public class EGLCore {
    public static final int FLAG_RECORDABLE = 0x01;

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private static final String TAG = "EGLCore";

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;

    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;

    private EGLConfig mEGLConfig = null;


    /**
     * 第一步 初始化
     *
     * @param eglContext
     * @param flag
     */
    public void init(EGLContext eglContext, int flag,int glVersion) {
        Log.d(TAG,"init glVersion:" + glVersion);

        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGLSurface has already");
        }


        EGLContext sharedContext = eglContext == null ? EGL14.EGL_NO_CONTEXT : eglContext;

        //start >>>>>> 获取EGLDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("cant get avaliable display");
        }
        //end >>>>>> 获取EGLDisplay

        //start >>>>>> 初始化EGLDisplay
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw new RuntimeException("cant initialize");
        }
        //end >>>>>> 初始化EGLDisplay

        //start >>>>>> 创建EGLConfig
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            EGLConfig config = getConfig(flag, glVersion);
            if (config == null) throw new RuntimeException("unable to find a suitable config");
            int[] attr2List = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, glVersion, EGL14.EGL_NONE};
            EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attr2List, 0);
            mEGLConfig = config;
            mEGLContext = context;
        }
        //end >>>>>> 创建EGLConfig
    }

    /**
     * 获取可用的EGLConfig
     *
     * @param flag
     * @param version
     * @return
     */
    public EGLConfig getConfig(int flag, int version) {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
        if (version >= 3) {
            renderableType = renderableType | EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        }

        int[] attrList = new int[]{
                EGL14.EGL_RED_SIZE, 0,
                EGL14.EGL_GREEN_SIZE, 0,
                EGL14.EGL_BLUE_SIZE, 0,
                EGL14.EGL_ALPHA_SIZE, 0,
                EGL14.EGL_ALPHA_SIZE, 0,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,
                EGL14.EGL_NONE,
        };

        if ((flag & FLAG_RECORDABLE) != 0) {
            attrList[attrList.length - 3] = EGL_RECORDABLE_ANDROID;
            attrList[attrList.length - 2] = 1;
        }

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];

        if (!EGL14.eglChooseConfig(mEGLDisplay, attrList, 0, configs, 0, configs.length, numConfigs, 0)) {
            Log.d(TAG, "Unable to find RGB8888 / " + version + " EGLConfig");
            return null;
        }
        return configs[0];
    }

    public EGLSurface createWindowSurface(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new RuntimeException("Invalid surface " + surface);
        }

        int[] surfaceAttr = new int[]{EGL14.EGL_NONE};
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttr, 0);

        if (eglSurface == null) {
            throw new RuntimeException("Surface is null");
        }
        return eglSurface;
    }

    public EGLSurface createOffScreenSurface(int width, int height) {
        int[] surfaceAttrs = new int[]{EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE};

        EGLSurface surface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttrs, 0);
        if (surface == null) {
            throw new RuntimeException("Surface is null");
        }
        return surface;
    }

    public void makeCurrent(EGLSurface surface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGLDisplay is null, call init first");
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext)) {
            throw new RuntimeException(
                    "makeCurrent fail"
            );
        }
    }

    public void makeCurrent(EGLSurface eglDrawSurface, EGLSurface eglReadSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGLDisplay is null, call init first");
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglDrawSurface, eglReadSurface, mEGLContext)) {
            throw new RuntimeException("makeCurrent fail");
        }
    }

    public boolean swapBuffers(EGLSurface eglSurface) {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface);
    }

    public void setPresentationTime(EGLSurface eglSurface, long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs);
    }

    public void destroySurface(EGLSurface eglSurface) {
        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
    }

    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }

        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLConfig = null;
    }
}
