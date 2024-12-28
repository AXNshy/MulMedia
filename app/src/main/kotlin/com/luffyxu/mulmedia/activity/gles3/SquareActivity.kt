package com.luffyxu.mulmedia.activity.gles3

import android.os.Bundle
import com.luffyxu.base.activity.ComposeActivity
import com.luffyxu.base.ui.ComposeSurfaceView
import com.luffyxu.gles3.GLES3Renderer
import com.luffyxu.gles3.square.Square1Renderer
import com.luffyxu.gles3.square.SquareShader1
import com.luffyxu.gles3.video.VideoShader

class SquareActivity : ComposeActivity() {
    val TAG = "SquareActivity"

    val drawer: Square1Renderer = Square1Renderer(SquareShader1(this))
    val render: GLES3Renderer = GLES3Renderer(listOf(drawer), 3)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawer.setShader(VideoShader(this))
        binding.composeContainer.setContent {
            ComposeSurfaceView(render)
        }
    }
}