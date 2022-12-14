package com.luffyxu.mulmedia.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.lifecycleScope
import com.luffy.mulmedia.databinding.ActivityCameraBinding
import com.luffyxu.camera.CameraClient
import com.luffyxu.mulmedia.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraActivity :CameraBaseActivity() {

    lateinit var viewBinding :ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
    }

    override fun cameraAvailable(immiadate: Boolean) {
//        viewBinding.viewPager
    }
}