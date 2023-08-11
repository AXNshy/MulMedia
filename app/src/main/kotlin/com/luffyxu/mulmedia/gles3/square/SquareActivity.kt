package com.luffyxu.mulmedia.gles3.square

import android.os.Bundle
import com.luffy.mulmedia.activity.ComposeActivity
import com.luffyxu.mulmedia.gles3.GLES3Renderer
import com.luffyxu.mulmedia.gles3.video.VideoShader
import com.luffyxu.mulmedia.ui.ComposeSurfaceView

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