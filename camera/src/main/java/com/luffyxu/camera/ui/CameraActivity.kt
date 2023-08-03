package com.luffyxu.camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import com.luffyxu.base.ActivityBase
import com.luffyxu.camera.databinding.ActivityCameraBinding

class CameraActivity : ActivityBase() {

    lateinit var viewBinding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
    }
}