package com.luffyxu.camera

import android.hardware.HardwareBuffer
import android.opengl.GLES30
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.luffyxu.opengles.base.egl.NativeRender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraNativeRender(val cameraClient: CameraClient) : SurfaceHolder.Callback {

    private val TAG = "CameraNativeRender"
    private val internalRender: NativeRender
    private var surfaceView: CameraPreviewView? = null
    var holder: SurfaceHolder? = null
        private set

    val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    var size: Size? = null

    init {
        internalRender = NativeRender()
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView as CameraPreviewView
        surfaceView.holder.addCallback(this)
        surfaceView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {}
        })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated")
        this.holder = holder
        scope.launch {
            cameraClient.startCameraWithEffect(surfaceView as SurfaceView)
        }
        internalRender.run()
        internalRender.onSurfaceCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged")
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        GLES30.glClearDepthf(0f)
        internalRender.onSurfaceChanged(holder.surface, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceChanged")
        internalRender.onSurfaceDestroyed()
    }

    fun updateImageBuffer(buffer: HardwareBuffer?) {
        Log.d(TAG, "updateImageBuffer $buffer")
        if (buffer == null) {
            return
        }
        internalRender.updateImageBuffer(buffer)
    }
}