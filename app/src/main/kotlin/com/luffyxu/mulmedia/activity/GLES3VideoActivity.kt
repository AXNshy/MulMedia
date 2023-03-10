package com.luffyxu.mulmedia.activity

import android.net.Uri
import android.opengl.GLES30
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.activity.BaseActivity
import com.luffy.mulmedia.codec.AudioDecoder
import com.luffy.mulmedia.codec.DecoderStateListener
import com.luffy.mulmedia.codec.VideoDecoder
import com.luffy.mulmedia.databinding.ActivityGl3VideoBinding
import com.luffy.mulmedia.utils.OpenGLUtils
import com.luffyxu.mulmedia.gles3.RenderThread
import com.luffyxu.mulmedia.gles3.VideoDrawer
import com.luffyxu.mulmedia.gles3.VideoShader
import java.io.FileDescriptor
import java.util.concurrent.Executors

class GLES3VideoActivity : BaseActivity() {
    val TAG = "GLES3VideoActivity"
    lateinit var binding: ActivityGl3VideoBinding
    lateinit var surfaceView: SurfaceView
    val thread : RenderThread = RenderThread()
//    val drawer : TriangleDrawer = TriangleDrawer()
    val mVideoDrawer : VideoDrawer = VideoDrawer()
    private val mExecutor = Executors.newFixedThreadPool(2)
    var mVideoDecoder: VideoDecoder? = null
    var mAudioDecoder: AudioDecoder? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gl_3_video)
        surfaceView = binding.glSurfaceview
        mVideoDrawer.setShader(VideoShader(this))
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2{
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceCreated")
                thread.surface = holder.surface

                thread.addDrawer(mVideoDrawer)
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
                mVideoDrawer.setSurfaceSize(width,height)
                mVideoDrawer.setTextureId(OpenGLUtils.GLES30_createTextureId(1)[0])
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(TAG,"surfaceDestroyed")
            }

            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }
        })

    }

    override fun onUriAction(uri: Uri?) {
        TODO("Not yet implemented")
    }

    override fun onUriAction(uri: FileDescriptor?) {
        TODO("Not yet implemented")
    }


    private fun initPlayer(path: Uri?, surface: Surface) {
        if (path == null || TextUtils.isEmpty(path.toString())) return
        mVideoDecoder = VideoDecoder(path.path, null, surface)
        mVideoDecoder!!.setStateListener(object : DecoderStateListener() {})
        mVideoDecoder!!.setVideoListener { width, height ->
            mVideoDrawer.setVideoSize(
                width,
                height
            )
        }
        mAudioDecoder = AudioDecoder(path.toString())
        mAudioDecoder!!.setStateListener(DecoderStateListener())
        mExecutor.execute(mVideoDecoder)
        mExecutor.execute(mAudioDecoder)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mVideoDecoder != null) {
            mVideoDecoder!!.stop()
        }
        if (mAudioDecoder != null) {
            mAudioDecoder!!.stop()
        }
    }
}