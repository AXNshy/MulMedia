package com.luffyxu.mulmedia.gles3.video

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Surface
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.activity.BaseActivity
import com.luffy.mulmedia.codec.AudioDecoder
import com.luffy.mulmedia.codec.DecoderStateListener
import com.luffy.mulmedia.codec.VideoDecoder
import com.luffy.mulmedia.databinding.ActivityGl3VideoBinding
import com.luffy.mulmedia.gles2.TextureCallback
import com.luffyxu.mulmedia.gles3.egl.GLES3Renderer
import java.io.FileDescriptor
import java.util.concurrent.Executors

class GLES3VideoActivity : BaseActivity() {
    val TAG = "GLES3VideoActivity"
    lateinit var binding: ActivityGl3VideoBinding
//    lateinit var surfaceView: SurfaceView
//    val thread : RenderThread = RenderThread()
    val mVideoDrawer : VideoDrawer = VideoDrawer()
    val mVideoRender = GLES3Renderer(listOf(mVideoDrawer),3)
    private val mExecutor = Executors.newFixedThreadPool(2)
    var mVideoDecoder: VideoDecoder? = null
    var mAudioDecoder: AudioDecoder? = null

    private var mSurface: Surface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gl_3_video)

        mVideoDrawer.setShader(VideoShader(this))
        mVideoDrawer.callback = TextureCallback { surface -> mSurface = Surface(surface) }

        mVideoRender.setSurfaceView(binding.glSurfaceview)

        binding.btnPlay.setOnClickListener {
            initPlayer(path,mSurface!!)
        }
    }

    override fun onUriAction(uri: Uri?) {
    }

    override fun onUriAction(uri: FileDescriptor?) {
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