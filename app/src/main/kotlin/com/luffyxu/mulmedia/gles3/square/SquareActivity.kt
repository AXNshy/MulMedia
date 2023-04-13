package com.luffyxu.mulmedia.gles3.square

import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.luffy.mulmedia.R
import com.luffy.mulmedia.databinding.ActivityGl3Binding
import com.luffyxu.mulmedia.gles3.GLES3Renderer
import com.luffyxu.mulmedia.gles3.video.VideoShader

class SquareActivity : AppCompatActivity(R.layout.activity_gl3) {
    val TAG = "SquareActivity"
    lateinit var binding: ActivityGl3Binding
    lateinit var surfaceView: SurfaceView

    val drawer: Square1Renderer = Square1Renderer(SquareShader1(this))
    val renderer: GLES3Renderer = GLES3Renderer(listOf(drawer), 3)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gl3)
        surfaceView = binding.glSurfaceview
        drawer.setShader(VideoShader(this))

        renderer.setSurfaceView(binding.glSurfaceview)
    }
}