package com.luffyxu.mulmedia.activity

import android.opengl.*
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.databinding.ActivityGl3VideoBinding
import com.luffy.mulmedia.opengl.TriangleDrawer
import com.luffyxu.mulmedia.gles.GLRenderer
import com.luffyxu.mulmedia.gles.RenderThread
import javax.microedition.khronos.egl.EGL10

class GLES3VideoActivity : AppCompatActivity() {
    val TAG = "GLES3VideoActivity"
    lateinit var binding: ActivityGl3VideoBinding
    lateinit var surfaceView: SurfaceView
    val thread : RenderThread = RenderThread()
    val drawer : GLRenderer = GLRenderer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gl_3_video)
        surfaceView = binding.glSurfaceview
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2{
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceCreated")
                thread.surface = holder.surface
                thread.addDrawer(drawer)
                thread.start()
                GLES30.glClear(0)

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(TAG,"surfaceChanged")
                GLES30.glClearColor(1f,1f,1f,1f)
                GLES30.glClearDepthf(0f)
                drawer.setSurfaceSize(width,height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceDestroyed")
            }

            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }
        })

    }

}