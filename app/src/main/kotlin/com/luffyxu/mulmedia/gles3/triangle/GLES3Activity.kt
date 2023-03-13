package com.luffyxu.mulmedia.gles3.triangle

import android.net.Uri
import android.opengl.GLES30
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.activity.BaseActivity
import com.luffy.mulmedia.databinding.ActivityGl3Binding
import com.luffy.mulmedia.databinding.ActivityGl3VideoBinding
import com.luffyxu.mulmedia.gles3.egl.GLES3Renderer
import com.luffyxu.mulmedia.gles3.egl.RenderThread
import com.luffyxu.mulmedia.gles3.video.VideoShader
import java.io.FileDescriptor

class GLES3Activity : BaseActivity() {
    val TAG = "GLES3Activity"
    lateinit var binding: ActivityGl3Binding
    lateinit var surfaceView: SurfaceView
//    val thread : RenderThread = RenderThread()

    val drawer : TriangleDrawer = TriangleDrawer()
    val mVideoRender = GLES3Renderer(listOf(drawer),3)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gl3)
        surfaceView = binding.glSurfaceview
        drawer.setShader(VideoShader(this))

        mVideoRender.setSurfaceView(binding.glSurfaceview)
//        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2{
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                Log.d(TAG,"surfaceCreated")
//                thread.surface = holder.surface
//
//                thread.addDrawer(drawer)
//                thread.start()
//                GLES30.glClear(0)
//
//            }
//
//            override fun surfaceChanged(
//                    holder: SurfaceHolder,
//                    format: Int,
//                    width: Int,
//                    height: Int
//            ) {
//                Log.d(TAG,"surfaceChanged")
//                GLES30.glClearColor(1f,1f,1f,1f)
//                GLES30.glClearDepthf(0f)
//                drawer.setSurfaceSize(width,height)
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                Log.d(TAG,"surfaceDestroyed")
//            }
//
//            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
//            }
//        })

    }

    override fun onUriAction(uri: Uri?) {
    }

    override fun onUriAction(uri: FileDescriptor?) {
    }
}