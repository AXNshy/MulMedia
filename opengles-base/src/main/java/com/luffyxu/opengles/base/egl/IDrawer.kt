package com.luffyxu.opengles.base.egl

interface IDrawer {
    fun draw()
    fun setTextureId(id: IntArray)
    fun release()
    fun setSurfaceSize(w: Int, h: Int)
    fun setVideoSize(w: Int, h: Int)
    fun translate(translateX: Float, translateY: Float)
    fun scale(scaleX: Float, scaleY: Float)
    fun setShader(shader: IGLShader)
}