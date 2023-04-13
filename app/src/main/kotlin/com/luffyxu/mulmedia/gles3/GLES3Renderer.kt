package com.luffyxu.mulmedia.gles3

import android.opengl.GLES30
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.luffyxu.opengles.base.egl.IDrawer
import com.luffyxu.opengles.base.egl.RenderThread2

class GLES3Renderer(val drawers: List<IDrawer>, glVersion: Int) : SurfaceHolder.Callback {

    private val TAG = "GLES3Renderer"
    private val mThread: RenderThread2
    private var surfaceView: SurfaceView? = null
    var holder: SurfaceHolder? = null
        private set

    init {
        mThread = RenderThread2()
        for(drawer in drawers){
            mThread.drawers.add(drawer)
        }
        mThread.start()
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.holder = holder
        mThread.surface = holder.surface
        mThread.onSurfaceCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG,"surfaceChanged")
        GLES30.glClearColor(1f,1f,1f,1f)
        GLES30.glClearDepthf(0f)
        mThread.onSurfaceChanged(width,height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread.onSurfaceDestroy()
    }
}