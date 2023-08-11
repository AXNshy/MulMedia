package com.luffyxu.mulmedia.ui

import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@Preview
@Composable
fun ComposeSurfaceView(callback: SurfaceHolder.Callback? = null) {
    AndroidView(factory = { context ->
        SurfaceView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            holder.addCallback(callback)
        }
    })
}