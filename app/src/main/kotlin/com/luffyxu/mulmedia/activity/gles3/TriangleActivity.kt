package com.luffyxu.mulmedia.activity.gles3

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import com.luffyxu.base.activity.ComposeActivity
import com.luffyxu.base.ui.ComposeSurfaceView
import com.luffyxu.gles3.GLES3Renderer
import com.luffyxu.gles3.triangle.TriangleDrawer
import com.luffyxu.gles3.video.VideoShader
import com.luffyxu.mulmedia.ui.PlaybackControllerClickType
import com.luffyxu.mulmedia.ui.PlaybackControllerState
import com.luffyxu.mulmedia.ui.SimpleControllerHorizontal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.FileDescriptor

class TriangleActivity : ComposeActivity() {
    val TAG = "GLES3Activity"

    val drawer: TriangleDrawer = TriangleDrawer()
    val render = GLES3Renderer(listOf(drawer), 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawer.setShader(VideoShader(this))
        binding.composeContainer.setContent {
            ComposeSurfaceView(render)
            SimpleControllerHorizontal(getPlaybackState().collectAsState()) { type, state ->
                when (type) {
                    PlaybackControllerClickType.BACK -> {
                        finish()
                    }

                    PlaybackControllerClickType.PLAY -> {}
                    else -> {}
                }
            }
        }
    }

    override fun onUriAction(uri: Uri?) {
    }

    override fun onUriAction(uri: FileDescriptor?) {
    }

    fun getPlaybackState(): StateFlow<PlaybackControllerState> {
        return MutableStateFlow(PlaybackControllerState(false, ""))
    }
}