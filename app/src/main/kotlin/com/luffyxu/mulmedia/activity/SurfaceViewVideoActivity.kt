package com.luffyxu.mulmedia.activity

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.activity.BaseActivity
import com.luffy.mulmedia.databinding.ActivitySurfaceBinding
import java.io.FileDescriptor

class SurfaceViewVideoActivity :BaseActivity(){
    val TAG = "SurfaceViewVideoActivity"
    lateinit var surfaceHolder: SurfaceHolder
    lateinit var surfaceView : SurfaceView
    lateinit var binding : ActivitySurfaceBinding

    var mediaPlayer : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_surface)
        surfaceView = binding.svVideo
        surfaceView.holder.addCallback(object :SurfaceHolder.Callback2{
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
                mediaPlayer = MediaPlayer().apply {
                    reset()
                    setSurface(surfaceHolder.surface)
                    setDataSource(path!!.path)
                    setOnPreparedListener {
                        it.start()
                    }
                    prepareAsync()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }
        })
        binding.btnRotate.setOnClickListener {
            surfaceView.rotation = surfaceView.rotation + 45
        }

        binding.btnTranslate.setOnClickListener {
            surfaceView.translationX =  surfaceView.translationX +10
        }
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
            release()
        }
    }
}