package com.luffyxu.opengles.base.egl

import android.opengl.EGLExt
import android.opengl.GLES20
import android.util.Log
import android.view.SurfaceHolder
import java.lang.ref.WeakReference

class RenderThread(val glVersion: Int = 2) : Thread() {
    private var mCurrentState = RenderState.NO_SURFACE
    private var mEGLHolder: EGLSurfaceHolder? = null
    private var mSurfaceHolder: WeakReference<SurfaceHolder>? = null
    private var isEGLCreated = false
    private var isBindEGLSurface = false
    private val waitLock = Object()
    var drawers: MutableList<IDrawer> = ArrayList()
    private var width = 0
    private var height = 0

    fun holdOn() {
        Log.d(TAG, "holdOn")
        synchronized(waitLock) {
            try {
                waitLock.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun notifyGo() {
        Log.d(TAG, "notifyGo")
        synchronized(waitLock) { waitLock.notify() }
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "onSurfaceCreated")
        mSurfaceHolder = WeakReference(holder)
        mCurrentState = RenderState.FRESH_SURFACE
        notifyGo()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged")
        this.width = width
        this.height = height
        mCurrentState = RenderState.SURFACE_CHANGE
        notifyGo()
    }

    fun onSurfaceDestroy() {
        Log.d(TAG, "onSurfaceDestroy")
        mCurrentState = RenderState.SURFACE_DESTROY
        notifyGo()
    }

    override fun run() {
        initEGL()
        while (true) {
            when (mCurrentState) {
                RenderState.RENDERING -> draw()
                RenderState.SURFACE_DESTROY -> {
                    destroyEGLSurface()
                    mCurrentState = RenderState.NO_SURFACE
                }
                RenderState.FRESH_SURFACE -> {
                    createEGLSurfaceFirst()
                    holdOn()
                }
                RenderState.SURFACE_CHANGE -> {
                    createEGLSurfaceFirst()
                    GLES20.glViewport(0, 0, width, height)
                    configWorldSize()
                    mCurrentState = RenderState.RENDERING
                }
                RenderState.STOP -> {
                    releaseEGL()
                    return
                }
                else -> holdOn()
            }
            try {
                sleep(20)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        Log.d(TAG, "release")
        mCurrentState = RenderState.NO_SURFACE
        notifyGo()
    }

    private fun initEGL() {
        Log.d(TAG, "initEGL")
        mEGLHolder = EGLSurfaceHolder()
        mEGLHolder!!.init(null, EGLExt.EGL_RECORDABLE_ANDROID, glVersion)
    }

    private fun createEGLSurfaceFirst() {
        Log.d(TAG, "createEGLSurfaceFirst")
        if (!isEGLCreated) {
            isEGLCreated = true
            createEGLSurface()
            if (!isBindEGLSurface) {
                isBindEGLSurface = true
                generateTextureId()
            }
        }
    }

    private fun generateTextureId() {
        Log.d(TAG, "generateTextureId")
//        val textureIds: IntArray = OpenGLUtils.createTextureId(drawers.size)
        var i = 0
//        Log.d(TAG, "generateTextureId " + Arrays.toString(textureIds))
        for (drawer in drawers) {
//            drawer.setTextureId(textureIds[i]);
            i++
        }
    }

    private fun createEGLSurface() {
        Log.d(TAG, "createEGLSurface")
        if (mSurfaceHolder != null) {
            mEGLHolder!!.createEGLSurface(mSurfaceHolder!!.get()!!.surface, -1, -1)
            mEGLHolder!!.makeCurrent()
        }
    }

    private fun configWorldSize() {
        Log.d(TAG, "configWorldSize")
        for (drawer in drawers) {
            drawer.setSurfaceSize(width, height)
        }
    }

    fun destroyEGLSurface() {
        Log.d(TAG, "destroyEGLSurface")
        mEGLHolder!!.destroyEGLSurface()
        isBindEGLSurface = false
    }

    fun releaseEGL() {
        Log.d(TAG, "releaseEGL")
        mEGLHolder!!.release()
    }

    private fun draw() {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        for (drawer in drawers) {
            drawer.draw()
        }
        mEGLHolder!!.swapBuffer()
    }

    companion object {
        const val TAG = "RenderThread"
    }
}