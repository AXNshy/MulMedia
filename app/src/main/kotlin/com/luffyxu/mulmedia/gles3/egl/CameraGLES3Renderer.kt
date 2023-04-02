package com.luffyxu.mulmedia.gles3.egl

import android.opengl.GLES30
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.luffy.mulmedia.gles2.IDrawer
import com.luffyxu.camera.AutoFitSurfaceView
import com.luffyxu.camera.CameraClient
import com.luffyxu.camera.getPreviewSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraGLES3Renderer(
    val drawers: List<IDrawer>,
    val glVersion: Int,
    val cameraClient: CameraClient
) : SurfaceHolder.Callback {

    private val TAG = "GLES3Renderer"
    private val mThread: RenderThread
    private var surfaceView: AutoFitSurfaceView? = null
    var holder: SurfaceHolder? = null
        private set

    val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    var size: Size? = null

    init {

        mThread = RenderThread()
        for (drawer in drawers) {
            mThread.addDrawer(drawer)
        }
        mThread.start()
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView as AutoFitSurfaceView
        surfaceView.holder.addCallback(this)
        surfaceView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
//                mThread.r()
            }
        })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.holder = holder
        mThread.surface = holder.surface
        mThread.onSurfaceCreated(holder)


        scope.launch {
            withContext(Dispatchers.Main) {
                val size = findSuitablePreviewSize()
            }
//            cameraClient.init(holder/*,size*/)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged")
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        GLES30.glClearDepthf(0f)
        mThread.onSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread.onSurfaceDestroy()
    }

    fun stop() {

    }

    private fun findSuitablePreviewSize(): Size {
        val previewSize = getPreviewSize(
            surfaceView!!.display,
            cameraClient.characteristics,
            SurfaceHolder::class.java
        )
        Log.d(TAG, "View finder size: ${surfaceView!!.width} x ${surfaceView!!.height}")
        Log.d(TAG, "Selected preview size: $previewSize")
        surfaceView!!.setAspectRatio(
            previewSize.width,
            previewSize.height
        )
        return previewSize
    }

}