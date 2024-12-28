package com.luffyxu.camera.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.luffyxu.base.activity.ActivityBase
import com.luffyxu.camera.databinding.ActivityCameraBinding

class CameraActivity : ActivityBase() {
    companion object {
        const val TAG = "CameraActivity"
    }

    lateinit var viewBinding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        viewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}