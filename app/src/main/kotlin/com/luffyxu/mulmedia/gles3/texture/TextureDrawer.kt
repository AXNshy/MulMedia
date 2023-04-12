package com.luffyxu.mulmedia.gles3.texture

import android.opengl.GLES30
import android.util.Log
import com.luffy.mulmedia.gles2.IDrawer
import com.luffy.mulmedia.gles2.IGLShader
import com.luffy.mulmedia.utils.OpenGLUtils
import com.luffyxu.camera.CameraClient
import com.luffyxu.mulmedia.gles3.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

class TextureDrawer : IDrawer {
    private var textureIds: IntArray? = null
    val TAG = "TextureDrawer"
    private val vertexCoordinate = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

//    private val vertexCoordinate = floatArrayOf(
//        -1f, 1f,
//        -1f, -1f,
//        1f, 1f,
//        1f, -1f,
//    )

//    private val textureCoordinate = floatArrayOf(
//        0f, 1f,
//        1f, 1f,
//        0f, 0f,
//        1f, 0f
//    )

    private val textureCoordinate = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f,
    )

    private var mMVPMatHandle = -1

    private var yTextureHandle = -1
    private var uTextureHandle = -1
    private var vTextureHandle = -1

    private var mProgramId = -1

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureCoordBuffer: FloatBuffer
    private var yTexBuffer: ByteBuffer? = null
    private var uTexBuffer: ByteBuffer? = null
    private var vTexBuffer: ByteBuffer? = null

    private var yTextureId = -1
    private var yTextureWidth = -1
    private var yTextureHeight = -1

    private var uTextureId = -1
    private var uTextureWidth = -1
    private var uTextureHeight = -1

    private var vTextureId = -1
    private var vTextureWidth = -1
    private var vTextureHeight = -1

    var mShader: IGLShader? = null

    private var mSurfaceWidth = 1
    private var mSurfaceHeight = 1

    private var mImageWidth = 1
    private var mImageHeight = 1

    private var mMatrix: FloatArray? = null
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val sizeRatio = FloatArray(2)

    init {
        createProgram()
    }

    fun setupCameraClient(cameraClient: CameraClient) {
        Log.d(TAG, "setupCameraClient")
        cameraClient.previewFrameCallback = { image, data, width, height ->
//            if(yTexBuffer== null) {
            Log.d(TAG, "previewFrameCallback 1 image.size:  ${image.planes.size}")
//            val bytes = CameraUtils.YUV_420_888_dataFetch(image)
            val ybytes =
                image.planes[0].buffer
            val ubytes =
                image.planes[1].buffer
            val vbytes =
                image.planes[2].buffer

            yTexBuffer = ybytes

            val ub = ByteArray(ubytes.limit() - ubytes.position())
            val vb = ByteArray(vbytes.limit() - vbytes.position())
            ubytes.get(ub)
            vbytes.get(vb)


            val uplane = image.planes[1]
            Log.d(TAG, "uplane.pixelStride ${uplane.pixelStride}")

            if (uplane.pixelStride == 1) {
                uTexBuffer = ByteBuffer.wrap(ub)
                vTexBuffer = ByteBuffer.wrap(vb)
            } else {
                val temp_u = ByteArray((ub.size + 1) / 2)
                val temp_v = ByteArray((vb.size + 1) / 2)

                var index_u = 0;
                var index_v = 0
                for (i in 0 until ub.size) {
                    if (i % 2 == 0) {
                        temp_u[index_u] = ub[i]
                        index_u++
                    }
                }
                for (i in 0 until vb.size) {
                    if (i % 2 == 0) {
                        temp_v[index_v] = vb[i]
                        index_v++
                    }
                }

                uTexBuffer = ByteBuffer.wrap(temp_u)
                vTexBuffer = ByteBuffer.wrap(temp_v)
            }
            Log.d(
                TAG,
                "previewFrameCallback2 ${yTexBuffer?.remaining()} ${uTexBuffer?.remaining()} ${vTexBuffer?.remaining()}"
            )

            yTextureWidth = width
            yTextureHeight = height

            uTextureWidth = width / 2
            uTextureHeight = height / 2
//            uTextureHeight = height
//            uTextureHeight = height
//
            vTextureHeight = height / 2
            vTextureWidth = width / 2
//
//            vTextureHeight = height
//            vTextureWidth = height

            mImageWidth = width
            mImageHeight = height
//            }
            if (mMatrix == null) {
                mMatrix = FloatArray(16)
            }
//            Matrix.setIdentityM(mMatrix,0)
//            Matrix.rotateM(mMatrix,0,90f, 0f,0f,0f)

        }
    }

    private fun initialMatrix() {
        if (mMatrix != null) {
            return
        }
        mMatrix = FloatArray(16)
        var top = 1f
        var bottom = -1f
        val verScale: Float = mSurfaceHeight.toFloat() / mImageHeight.toFloat()
        val horScale: Float = mSurfaceWidth.toFloat() / mImageWidth.toFloat()
        if (horScale < verScale) {
            top = verScale / horScale * 2
            sizeRatio[1] = top
            bottom = -top
        }
        sizeRatio[0] = 2f
//        Matrix.orthoM(projectionMatrix, 0, -2f, 2f, bottom, top, 3f, 5f)
////        Matrix.rotateM(projectionMatrix,0,90f, 1f,0f,0f)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
//        Matrix.multiplyMM(mMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Log.v(TAG, "initialMatrix " + Arrays.toString(mMatrix))
    }


    private fun createProgram() {
        val buffer = ByteBuffer.allocateDirect(vertexCoordinate.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer.put(vertexCoordinate)
        vertexBuffer.position(0)
        //flip must be ahead of rotate operation
        resolveFlip(textureCoordinate, Transformation.FLIP_VERTICAL);
        resolveRotate(textureCoordinate, 90)
        val buffer1 = ByteBuffer.allocateDirect(textureCoordinate.size * 4)
        buffer1.order(ByteOrder.nativeOrder())
        textureCoordBuffer = buffer1.asFloatBuffer()
        textureCoordBuffer.put(textureCoordinate)
        textureCoordBuffer.position(0)

//        createGLPro();
    }

    private fun createGLPro() {
        if (mProgramId < 0) {
            Log.d(TAG, "createGLPro mProgramId:$mProgramId")
            val vertexShader = createShader(GLES30.GL_VERTEX_SHADER, mShader!!.vertexShader())
            val fragShader = createShader(GLES30.GL_FRAGMENT_SHADER, mShader!!.fragmentShader())
            mProgramId = GLES30.glCreateProgram()
            GLES30.glAttachShader(mProgramId, vertexShader)
            GLES30.glAttachShader(mProgramId, fragShader)
            GLES30.glLinkProgram(mProgramId)
            mMVPMatHandle = GLES30.glGetUniformLocation(mProgramId, "u_MVPMatrix")
            yTextureHandle = GLES30.glGetUniformLocation(mProgramId, "s_textureY")
            uTextureHandle = GLES30.glGetUniformLocation(mProgramId, "s_textureU")
            vTextureHandle = GLES30.glGetUniformLocation(mProgramId, "s_textureV")
            GLES30.glUseProgram(mProgramId)
        }
    }

    override fun draw() {
        Log.d(TAG, "draw")

        initialMatrix()

        createGLPro()
        activeTexture()
        bindTextureToImage()
        doDraw()
    }

    private fun bindTextureToImage() {
        Log.d(TAG, "bindTextureToImage")
        if (yTexBuffer != null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, yTextureId)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_LUMINANCE,
                yTextureWidth,
                yTextureHeight,
                0,
                GLES30.GL_LUMINANCE,
                GLES30.GL_UNSIGNED_BYTE,
                yTexBuffer
            )
        }

        if (uTexBuffer != null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, uTextureId)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_LUMINANCE,
                uTextureWidth,
                uTextureHeight,
                0,
                GLES30.GL_LUMINANCE,
                GLES30.GL_UNSIGNED_BYTE,
                uTexBuffer
            )
        }

        if (vTexBuffer != null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, vTextureId)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_LUMINANCE,
                vTextureWidth,
                vTextureHeight,
                0,
                GLES30.GL_LUMINANCE,
                GLES30.GL_UNSIGNED_BYTE,
                vTexBuffer
            )
        }
    }

    private fun activeTexture() {
        Log.d(TAG, "activeTexture")
        if (yTextureId == -1 || uTextureId == -1 || vTextureId == -1) {

            checkGLError("GLES30_createTextureId")
            textureIds = OpenGLUtils.GLES30_createTextureId(3)
            yTextureId = textureIds!![0]
            uTextureId = textureIds!![1]
            vTextureId = textureIds!![2]

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            checkGLError("glActiveTexture0")
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, yTextureId)
            checkGLError("glBindTexture0")
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR.toFloat()
            )
            checkGLError("glTexParameterf0")
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

            //U
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            checkGLError("glActiveTexture1")
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, uTextureId)
            checkGLError("glUniform1i1")
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

            //V
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            checkGLError("glActiveTexture2")
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, vTextureId)
            checkGLError("glUniform1i2")
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

    }


    private fun doDraw() {
        Log.d(TAG, "doDraw")
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)


        GLES30.glUniform1i(yTextureHandle, 0)
        GLES30.glUniform1i(uTextureHandle, 1)
        GLES30.glUniform1i(vTextureHandle, 2)

        GLES30.glUniformMatrix4fv(mMVPMatHandle, 1, false, mMatrix, 0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textureCoordBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    override fun setTextureId(id: IntArray) {

    }

    override fun release() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, textureIds, 0)
        GLES30.glDeleteProgram(mProgramId)
    }

    override fun setSurfaceSize(w: Int, h: Int) {
        mSurfaceWidth = w
        mSurfaceHeight = h
    }

    override fun setVideoSize(w: Int, h: Int) {
        mImageWidth = w
        mImageHeight = h
    }

    override fun translate(translateX: Float, translateY: Float) {}

    override fun scale(scaleX: Float, scaleY: Float) {}
    override fun setShader(shader: IGLShader?) {
        mShader = shader
    }
}