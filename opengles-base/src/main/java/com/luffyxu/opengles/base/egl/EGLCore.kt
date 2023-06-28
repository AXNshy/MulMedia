package com.luffyxu.opengles.base.egl

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface

class EGLCore {
    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null

    /**
     * 第一步 初始化
     *
     * @param eglContext
     * @param flag
     */
    fun init(eglContext: EGLContext?, flag: Int, glVersion: Int) {
        Log.d(TAG, "init glVersion:$glVersion")
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLSurface has already")
        }
        val sharedContext = eglContext ?: EGL14.EGL_NO_CONTEXT

        //start >>>>>> 获取EGLDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("cant get avaliable display")
        }
        //end >>>>>> 获取EGLDisplay

        //start >>>>>> 初始化EGLDisplay
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw RuntimeException("cant initialize")
        }
        //end >>>>>> 初始化EGLDisplay

        //start >>>>>> 创建EGLConfig
        if (mEGLContext === EGL14.EGL_NO_CONTEXT) {
            val config = getConfig(flag, glVersion)
                ?: throw RuntimeException("unable to find a suitable config")
            val attr2List = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, glVersion, EGL14.EGL_NONE)
            val context = EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attr2List, 0)
            mEGLConfig = config
            mEGLContext = context
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
    fun getConfig(flag: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }
        val attrList = intArrayOf(
            EGL14.EGL_RED_SIZE, 0,
            EGL14.EGL_GREEN_SIZE, 0,
            EGL14.EGL_BLUE_SIZE, 0,
            EGL14.EGL_ALPHA_SIZE, 0,
            EGL14.EGL_ALPHA_SIZE, 0,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,
            EGL14.EGL_NONE
        )
        if (flag and FLAG_RECORDABLE != 0) {
            attrList[attrList.size - 3] = EGL_RECORDABLE_ANDROID
            attrList[attrList.size - 2] = 1
        }
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(
                mEGLDisplay,
                attrList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            Log.d(TAG, "Unable to find RGB8888 / $version EGLConfig")
            return null
        }
        return configs[0]
    }

    fun createWindowSurface(surface: Any): EGLSurface {
        if (surface !is Surface && surface !is SurfaceTexture) {
            throw RuntimeException("Invalid surface $surface")
        }
        val surfaceAttr = intArrayOf(EGL14.EGL_NONE)
        return EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttr, 0)
            ?: throw RuntimeException("Surface is null")
    }

    fun createOffScreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttrs =
            intArrayOf(EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE)
        return EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttrs, 0)
            ?: throw RuntimeException("Surface is null")
    }

    fun makeCurrent(surface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay is null, call init first")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext)) {
            throw RuntimeException(
                "makeCurrent fail"
            )
        }
    }

    fun makeCurrent(eglDrawSurface: EGLSurface?, eglReadSurface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay is null, call init first")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglDrawSurface, eglReadSurface, mEGLContext)) {
            throw RuntimeException("makeCurrent fail")
        }
    }

    fun swapBuffers(eglSurface: EGLSurface?): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    fun setPresentationTime(eglSurface: EGLSurface?, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    fun destroySurface(eglSurface: EGLSurface?) {
        EGL14.eglMakeCurrent(
            mEGLDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
    }

    fun release() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }

    companion object {
        const val FLAG_RECORDABLE = 0x01
        private const val EGL_RECORDABLE_ANDROID = 0x3142
        private const val TAG = "EGLCore"
    }
}