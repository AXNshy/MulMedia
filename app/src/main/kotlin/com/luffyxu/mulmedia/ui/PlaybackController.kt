package com.luffyxu.mulmedia.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luffy.mulmedia.R
import com.luffyxu.mulmedia.util.TimerState


@Preview
@Composable
fun PlaybackControllerUI(callback: PlaybackControllerCallback = DefaultControllerCallback()) {
    Row(
        modifier = Modifier
            .background(colorResource(id = android.R.color.transparent))
            .size(
                1080.dp,
                80.dp
            )
            .absolutePadding(top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ActionPlay(mode = 0, onValueChanged = {}, callback = callback::onPlayOrPause)
    }

}


@Composable
fun ActionPlay(mode: Int, onValueChanged: (Int) -> Unit, callback: () -> Unit? = { }) {
    Image(painter = painterResource(id = R.drawable.icon_play), contentDescription = "play",
        alignment = Alignment.Center, modifier = Modifier
            .size(48.dp)
            .clickable {
                callback.invoke()
            })
}


interface PlaybackControllerCallback {
    fun onPlayOrPause()
}

class DefaultControllerCallback : PlaybackControllerCallback {
    override fun onPlayOrPause() {
    }
}


enum class PlaybackControllerClickType {
    BACK,
    PLAY,
    SEEK
}

@Preview
@Composable
fun SimpleControllerHorizontal(
    state: State<PlaybackControllerState> =
        mutableStateOf(PlaybackControllerState(false, name = "Title")),
    callback: (PlaybackControllerClickType, PlaybackControllerState) -> Unit = { _, _ -> }
) {
    var isplaying by remember { mutableStateOf(state.value.isPlaying) }
    var showController by remember { mutableStateOf(true) }
    var controllerTimer = TimerState("showController") {
        showController = false
    }
    controllerTimer.start()

    if (showController) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .background(Color.Transparent)
                .clickable {
                    showController = true
                    controllerTimer.start()
                }
        ) {
            Row(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth(1f)
                    .background(Color(0x05000000)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_48px),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(start = 18.dp, end = 18.dp)
                        .size(64.dp)
                        .clip(shape = RoundedCornerShape(20.dp))
                        .border(width = 0.dp, Color.Transparent)
                        .clickable {
                            callback(PlaybackControllerClickType.BACK, state.value)
                        },

                    )

                Text(text = state.value.name, color = colorResource(id = R.color.white))
            }

            Image(
                painter = painterResource(
                    id = if (isplaying) {
                        R.drawable.icon_pause
                    } else {
                        R.drawable.icon_play
                    }
                ),

                contentDescription = "",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                    ) {

                        isplaying = !isplaying
                        callback(PlaybackControllerClickType.PLAY, state.value)
                    },

                alignment = Alignment.Center,
            )
        }
    }
}

data class PlaybackControllerState(
    var isPlaying: Boolean = false,
    var name: String = "",
)
