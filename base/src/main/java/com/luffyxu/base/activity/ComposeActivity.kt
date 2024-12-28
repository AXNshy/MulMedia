package com.luffyxu.base.activity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import com.luffyxu.base.databinding.ActivityComposeBinding
import java.io.FileDescriptor

abstract class ComposeActivity : BaseActivity() {

    lateinit var binding: ActivityComposeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }

    override fun onUriAction(uri: Uri?) {

    }

    override fun onUriAction(uri: FileDescriptor?) {
        TODO("Not yet implemented")
    }
}