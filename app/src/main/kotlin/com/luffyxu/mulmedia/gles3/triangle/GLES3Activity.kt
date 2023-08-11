package com.luffyxu.mulmedia.gles3.triangle

import android.net.Uri
import android.os.Bundle
import com.luffy.mulmedia.activity.ComposeActivity
import com.luffyxu.mulmedia.gles3.GLES3Renderer
import com.luffyxu.mulmedia.gles3.video.VideoShader
import com.luffyxu.mulmedia.ui.ComposeSurfaceView
import java.io.FileDescriptor

class GLES3Activity : ComposeActivity() {
    val TAG = "GLES3Activity"

    val drawer: TriangleDrawer = TriangleDrawer()
    val render = GLES3Renderer(listOf(drawer), 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawer.setShader(VideoShader(this))
        binding.composeContainer.setContent {
            ComposeSurfaceView(render)
        }
    }

    override fun onUriAction(uri: Uri?) {
    }

    override fun onUriAction(uri: FileDescriptor?) {
    }
}