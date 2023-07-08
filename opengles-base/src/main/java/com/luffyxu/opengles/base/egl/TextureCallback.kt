package com.luffyxu.opengles.base.egl

import android.graphics.SurfaceTexture

interface TextureCallback {
    fun texture(surface: SurfaceTexture)
}