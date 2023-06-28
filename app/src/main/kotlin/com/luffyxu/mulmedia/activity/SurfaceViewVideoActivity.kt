package com.luffyxu.mulmedia.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.activity.BaseActivity
import com.luffy.mulmedia.databinding.ActivitySurfaceBinding
import com.luffyxu.ffmpeg.FFmpegPlayer
import java.io.FileDescriptor

class SurfaceViewVideoActivity :BaseActivity(){
    val TAG = "SurfaceViewVideoActivity"
    lateinit var surfaceHolder: SurfaceHolder
    lateinit var surfaceView : SurfaceView
    lateinit var binding : ActivitySurfaceBinding

    var mediaPlayer : FFmpegPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_surface)
        surfaceView = binding.svVideo
        if (path != null) {
            mediaPlayer = FFmpegPlayer(path!!.toString())
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surfaceHolder = holder
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                holder.setFixedSize(width, height)
                mediaPlayer?.apply {
                    createPlayer(holder.surface)
                    play()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }
        })
    }

    override fun onUriAction(uri: Uri?) {
        Log.d(TAG,"onUriAction $uri")

    }

    override fun onUriAction(uri: FileDescriptor?) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.apply {
            stop()
        }
    }
}