package com.luffyxu.mulmedia.gles3.texture

import android.media.Image
import android.opengl.GLES30
import android.util.Log
import com.luffy.mulmedia.gles2.IDrawer
import com.luffy.mulmedia.gles2.IGLShader
import com.luffyxu.camera.CameraClient
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureDrawer : IDrawer {
    val TAG = "TextureDrawer"
    private val vertexCoordinate = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    private val textureCoordinate = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    private var mMVPMatHandle = -1

    private var textureHandle = -1

    private var mProgramId = -1

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureCoordBuffer: FloatBuffer
    private var texBuffer: ByteBuffer? = null

    private lateinit var mImage: Image
    private var textureId = -1

    var mShader: IGLShader? = null

    var cameraClient: CameraClient? = null

    init {
        createProgram()
    }

    fun setupCameraClient(cameraClient: CameraClient) {
        this.cameraClient = cameraClient

        Log.d(TAG, "setupCameraClient")
        cameraClient.previewFrameCallback = { data, width, height ->
            Log.d(TAG, "previewFrameCallback $width $height")
//            texBuffer = ByteBuffer.allocateDirect(data.size * 4)
//                .apply {
//                    order(ByteOrder.nativeOrder())
//                    put(data)
//                    position(0)
//                }
            texBuffer = data
        }
    }

    private fun createProgram() {
        val buffer = ByteBuffer.allocateDirect(vertexCoordinate.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer.put(vertexCoordinate)
        vertexBuffer.position(0)
        val buffer1 = ByteBuffer.allocateDirect(textureCoordinate.size * 4)
        buffer1.order(ByteOrder.nativeOrder())
        textureCoordBuffer = buffer1.asFloatBuffer()
        textureCoordBuffer.put(textureCoordinate)
        textureCoordBuffer.position(0)

//        createGLPro();
    }

    private fun createGLPro() {
        val vertexShader = createShader(GLES30.GL_VERTEX_SHADER, mShader!!.vertexShader())
        val fragShader = createShader(GLES30.GL_FRAGMENT_SHADER, mShader!!.fragmentShader())
        mProgramId = GLES30.glCreateProgram()
        GLES30.glAttachShader(mProgramId, vertexShader)
        GLES30.glAttachShader(mProgramId, fragShader)
        GLES30.glLinkProgram(mProgramId)
        textureHandle = GLES30.glGetUniformLocation(mProgramId, "u_MVPMatrix")
        GLES30.glUseProgram(mProgramId)
    }

    private fun createShader(type: Int, code: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, code)
        GLES30.glCompileShader(shader)
        return shader
    }


    override fun draw() {
        Log.d(TAG, "draw")
        createGLPro()
        activeTexture()
        bindTextureToImage()
        doDraw()
    }

    private fun bindTextureToImage() {
        if (texBuffer != null) {
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                mImage.width,
                mImage.width,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                texBuffer
            )
        }
    }

    private fun activeTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(textureHandle, 0)
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
    }


    private fun doDraw() {
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textureCoordBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    override fun setTextureId(id: Int) {
        textureId = id
    }

    override fun release() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, IntArray(1), 0)
        GLES30.glDeleteProgram(mProgramId)
    }

    override fun setSurfaceSize(w: Int, h: Int) {}

    override fun setVideoSize(w: Int, h: Int) {}

    override fun translate(translateX: Float, translateY: Float) {}

    override fun scale(scaleX: Float, scaleY: Float) {}
    override fun setShader(shader: IGLShader?) {
        mShader = shader
    }
}