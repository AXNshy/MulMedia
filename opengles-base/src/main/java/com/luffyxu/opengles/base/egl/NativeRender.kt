package com.luffyxu.opengles.base.egl

import android.view.Surface

class NativeRender {
    var native_renderer: Int = -1;

    companion object {
        val TAG = "NativeRender"

        init {
            System.loadLibrary("gles_external")
        }
    }

    init {
        native_renderer = nativeCreateRenderer()
    }


    fun run() {
        nativeRun(native_renderer)
    }


    private external fun nativeCreateRenderer(): Int
    private external fun nativeRun(native_render: Int)

    fun onSurfaceCreated(surface: Surface) {
        onSurfaceCreated(native_renderer, surface)
    }

    private external fun onSurfaceCreated(native_render: Int, surface: Surface)
    fun onSurfaceChanged(surface: Surface, width: Int, height: Int) {
        onSurfaceChanged(native_renderer, surface, width, height)
    }

    private external fun onSurfaceChanged(
        native_render: Int,
        surface: Surface,
        width: Int,
        height: Int
    )

    fun onSurfaceDestroyed() {
        onSurfaceDestroyed(native_renderer)
    }

    private external fun onSurfaceDestroyed(native_render: Int)
}