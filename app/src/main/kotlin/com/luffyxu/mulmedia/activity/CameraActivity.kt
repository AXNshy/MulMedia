package com.luffyxu.mulmedia.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.lifecycleScope
import com.luffy.mulmedia.databinding.ActivityCameraBinding
import com.luffyxu.camera.CameraClient
import kotlinx.coroutines.launch

class CameraActivity :CameraBaseActivity() {
    lateinit var viewBinding : ActivityCameraBinding


    lateinit var cameraClient : CameraClient

    lateinit var surfaceHolder : SurfaceHolder
    lateinit var surface : Surface

    var handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"onCreate")
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.apply {
            btnCapture.setOnClickListener { _->
                lifecycleScope.launch {
                    val result = cameraClient.takePhoto()
                    cameraClient.savePhoto(result)
                }
            }
        }

        lifecycleScope.launch{
            cameraClient = CameraClient()
        }
    }

    override fun cameraAvailable(immiadate:Boolean) {
        Log.d(TAG,"cameraAvailable $immiadate")
//        if(immiadate){
            handler.post{
                viewBinding.surfacePreview.holder.addCallback(object : SurfaceHolder.Callback2 {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        Log.d(TAG,"surfaceCreated")
                        lifecycleScope.launch {
                            val inited = cameraClient.init(this@CameraActivity,holder.surface)
                            if(inited) {
                                cameraClient.openPreview()
                            }
                        }
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        Log.d(TAG,"surfaceChanged")
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        Log.d(TAG,"surfaceDestroyed")
                    }

                    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                    }
                })
            }
//        }

    }


    companion object{
        const val TAG = "CameraBase"
    }
}