package com.luffyxu.mulmedia.gles3.egl

import android.opengl.GLES30
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.luffy.mulmedia.gles2.IDrawer
import com.luffyxu.camera.CameraClient
import com.luffyxu.camera.CameraPreviewView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraGLES3Renderer(
    val drawers: List<IDrawer>,
    val glVersion: Int,
    val cameraClient: CameraClient
) : SurfaceHolder.Callback {

    private val TAG = "GLES3Renderer"
    private val mThread: RenderThread
    private var surfaceView: CameraPreviewView? = null
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
        this.surfaceView = surfaceView as CameraPreviewView
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
        scope.launch {
            cameraClient.startCameraWithEffect(surfaceView as SurfaceView)
        }

        mThread.surface = holder.surface
        mThread.onSurfaceCreated(holder)

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
}