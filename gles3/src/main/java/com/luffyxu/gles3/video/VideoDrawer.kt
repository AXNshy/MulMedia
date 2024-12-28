package com.luffyxu.gles3.video

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.luffyxu.mulmedia.gles3.createShader
import com.luffyxu.opengles.base.drawer.IDrawer
import com.luffyxu.opengles.base.egl.TextureCallback
import com.luffyxu.opengles.base.shader.IGLShader
import com.luffyxu.opengles.base.utils.OpenGLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Arrays

class VideoDrawer : IDrawer {

    val TAG = "GLES3-Video"
    private val vertexCoordinate = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f,
    )

    private val textureCoordinate = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    private var mProgramId = -1
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    private var textureId = -1

    private var mMatrixHandle = -1
    private var mTextureHandle = -1

    var mGLShader: IGLShader? = null

    var surfaceTexture: SurfaceTexture? = null

    private var mSurfaceWidth = 1
    private var mSurfaceHeight = 1

    private var mVideoWidth = 1
    private var mVideoHeight = 1

    private var mMatrix: FloatArray? = null
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)


    private val sizeRatio = FloatArray(2)

    private fun createProgram() {
        Log.d(TAG, "createProgram")
        val buffer = ByteBuffer.allocateDirect(vertexCoordinate.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer?.put(vertexCoordinate)
        vertexBuffer?.position(0)

        val buffer1 = ByteBuffer.allocateDirect(textureCoordinate.size * 4)
        buffer1.order(ByteOrder.nativeOrder())
        textureBuffer = buffer1.asFloatBuffer()
        textureBuffer?.put(textureCoordinate)
        textureBuffer?.position(0)
    }

    private fun initialMatrix() {
        if (mMatrix != null) {
            return
        }
        mMatrix = FloatArray(16)
        var top = 1f
        var bottom = -1f
        val verScale: Float = mSurfaceHeight.toFloat() / mVideoHeight.toFloat()
        val horScale: Float = mSurfaceWidth.toFloat() / mVideoWidth.toFloat()
        if (horScale < verScale) {
            top = verScale / horScale * 2
            sizeRatio[1] = top
            bottom = -top
        }
        sizeRatio[0] = 2f
        Matrix.orthoM(projectionMatrix, 0, -2f, 2f, bottom, top, 3f, 5f)
        //        Matrix.orthoM(projectionMatrix, 0, -2, 2, bottom, top, 1, 2);
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Log.v(TAG, "initialMatrix " + Arrays.toString(mMatrix))
    }

    private fun createGLPro() {
        if (mProgramId == -1) {
            Log.d(TAG, "createGLPro")
            val vertexShader =
                createShader(GLES30.GL_VERTEX_SHADER, mGLShader!!.vertexShader())

            val fragShader =
                createShader(GLES30.GL_FRAGMENT_SHADER, mGLShader!!.fragmentShader())
            mProgramId = GLES30.glCreateProgram()

            GLES30.glAttachShader(mProgramId, vertexShader)
            GLES30.glAttachShader(mProgramId, fragShader)
            GLES30.glLinkProgram(mProgramId)
            val path: MutableList<Int> = mutableListOf()
            val result: MutableList<List<Int>> = mutableListOf()
            path.add(1)
            result.add(path.toMutableList())
            mMatrixHandle = GLES30.glGetUniformLocation(mProgramId, "uMatrix")
            mTextureHandle = GLES30.glGetUniformLocation(mProgramId, "uTexture")
        }
    }

    override fun draw() {
        Log.d(TAG, "draw mProgramId:$mProgramId")
        initialMatrix()
        createGLPro()
        activeTexture()
        updateTexture()
        doDraw()
    }

    fun checkGLError(method: String) {
        var err: Int = GLES30.glGetError()
        if (err != GLES30.GL_NO_ERROR) {
            println("method($method) error($err)")
        }
    }

    private fun doDraw() {
        // Clear the color buffer
        GLES30.glUseProgram(mProgramId)

        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glUniform1i(mTextureHandle, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)


        GLES30.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    private fun activeTexture() {
        Log.d(TAG, "activeTexture $textureId")
        if (textureId == -1) {
            setTextureId(OpenGLUtils.createTextureId(1))
        }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

    }

    private fun updateTexture() {
        surfaceTexture?.updateTexImage()
        val error = GLES30.glGetError()
        if (error != 0) {
            Log.d(TAG, "updateTexImage error $error")
        }
    }

    override fun setTextureId(id: IntArray) {
        Log.d(TAG, "setTextureId $id")
        textureId = id[0]
        surfaceTexture = SurfaceTexture(textureId)
        if (callback != null) {
            callback!!.texture(surfaceTexture!!)
        }
    }

    var callback: TextureCallback? = null
        set(value) {
            field = value
            if (surfaceTexture != null && callback != null) {
                value?.texture(surfaceTexture!!)
            }
        }


    override fun translate(translateX: Float, translateY: Float) {}
    override fun scale(scaleX: Float, scaleY: Float) {}
    override fun setShader(shader: IGLShader) {
        mGLShader = shader
    }

    override fun release() {
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, IntArray(1), 0)
        GLES30.glDeleteProgram(mProgramId)
    }

    override fun setVideoSize(w: Int, h: Int) {
        Log.d(TAG, "setVideoSize w:$w,h:$h")
        mVideoWidth = w
        mVideoHeight = h
    }

    override fun setSurfaceSize(w: Int, h: Int) {
        Log.d(TAG, "setSurfaceSize w:$w,h:$h")
        mSurfaceWidth = w
        mSurfaceHeight = h
        GLES30.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight)
    }

    init {
        createProgram()
    }
}
