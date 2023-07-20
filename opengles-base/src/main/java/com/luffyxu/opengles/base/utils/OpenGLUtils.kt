package com.luffyxu.opengles.base.utils

import android.opengl.GLES20
import android.opengl.GLES30

object OpenGLUtils {
    @JvmStatic
    fun createTextureId(count: Int): IntArray {
        val arrys = IntArray(count)
        GLES20.glGenTextures(count, arrys, 0)
        return arrys
    }

    fun GLES30_createTextureId(count: Int): IntArray {
        val arrys = IntArray(count)
        GLES30.glGenTextures(count, arrys, 0)
        return arrys
    }

    @JvmStatic
    fun createFBOTexture(width: Int, height: Int): Int {
        //创建纹理id
        val textureId = IntArray(1)
        //生成纹理id
        GLES20.glGenTextures(1, textureId, 0)
        // bind texture id to texture unit
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        // bind the texture pixels to texture unit.
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        // texture parameter set
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )

        // unbind the texture id and texture unit
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureId[0]
    }

    @JvmStatic
    fun createFrameBuffer(): Int {
        val fbs = IntArray(1)
        GLES20.glGenBuffers(1, fbs, 0)
        return fbs[0]
    }

    @JvmStatic
    fun bindFBO(fb: Int, texture: Int) {
        // bind framebufferobject id to framebuffer unit
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        // bind texture unit with fb object to make the texture pixels can render to the fbo.
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, texture, 0
        )
    }

    @JvmStatic
    fun unbindFBO() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    @JvmStatic
    fun deleteFBO(fb: IntArray?, texture: IntArray?) {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glDeleteBuffers(1, fb, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, texture, 0)
    }
}