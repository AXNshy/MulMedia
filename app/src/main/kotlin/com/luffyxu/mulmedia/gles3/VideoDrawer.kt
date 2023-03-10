package com.luffyxu.mulmedia.gles3;

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.luffy.mulmedia.gles2.IDrawer
import com.luffy.mulmedia.gles2.IGLShader
import com.luffy.mulmedia.gles2.TextureCallback
import com.luffy.mulmedia.gles2.VideoDrawer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

class VideoDrawer : IDrawer {

    val TAG="GLRenderer"
    private val vertexCoordinate = floatArrayOf(
        -1f, -1f,0f,
        1f, -1f,0f,
        -1f, 1f,0f,
        1f, 1f,0f,
    )

    private val textureCoordinate=floatArrayOf(
        0f,1f,
        1f,1f,
        0f,0f,
        1f,0f
    )
    private val color=floatArrayOf(
        1f,0f,0f,1f,1f,1f
    )


    private var mProgramId=-1
    private var vertexBuffer: FloatBuffer?=null
    private var textureBuffer:FloatBuffer?=null
    private var colorBuffer:FloatBuffer?=null
    private var textureId=0

    private var mMatrixHandle=-1;
    private var positionHandle=-1;
    private var mCoordinateHandle = -1
    private var mTextureHandle = -1

    var mGLShader: IGLShader? = null

    var surfaceTexture:SurfaceTexture? = null

    private var mSurfaceWidth = 1
    private var mSurfaceHeight = 1

    private var mVideoWidth = 1
    private var mVideoHeight = 1

    private var mMatrix: FloatArray? = null
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)


    private val sizeRatio = FloatArray(2)

    private fun createProgram(){
        Log.d(TAG,"createProgram")
        val buffer=ByteBuffer.allocateDirect(vertexCoordinate.size*4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer=buffer.asFloatBuffer()
        vertexBuffer?.put(vertexCoordinate)
        vertexBuffer?.position(0)
        val buffer1= ByteBuffer.allocateDirect(textureCoordinate.size*4)
        buffer1.order(ByteOrder.nativeOrder())
        textureBuffer=buffer1.asFloatBuffer()
        textureBuffer?.put(textureCoordinate)
        textureBuffer?.position(0)

        val buffer2=ByteBuffer.allocateDirect(color.size*4)
        buffer2.order(ByteOrder.nativeOrder())
        colorBuffer=buffer2.asFloatBuffer().apply{
            put(color)
            position(0)
        }
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
        Log.v(VideoDrawer.TAG, "initialMatrix " + Arrays.toString(mMatrix))
    }

    private fun createGLPro(){
        if(mProgramId==-1){
            Log.d(TAG,"createGLPro")
            val vertexShader=
                createShader(GLES30.GL_VERTEX_SHADER,mGLShader!!.vertexShader())
            val fragShader=
                createShader(GLES30.GL_FRAGMENT_SHADER,mGLShader!!.fragmentShader())
            mProgramId=GLES30.glCreateProgram()

            GLES30.glBindAttribLocation(mProgramId,mCoordinateHandle,"aCoordinate")
            GLES30.glBindAttribLocation(mProgramId,positionHandle,"vPosition")
            GLES30.glBindAttribLocation(mProgramId,mMatrixHandle,"uMatrix")



            GLES30.glAttachShader(mProgramId,vertexShader)
            GLES30.glAttachShader(mProgramId,fragShader)
            GLES30.glLinkProgram(mProgramId)


            mTextureHandle = GLES30.glGetUniformLocation(mProgramId,"uTexture")
//        vertexHandle = GLES30.glGetAttribLocation(mProgramId, "aPosition")
//        textureHandle = GLES30.glGetAttribLocation(mProgramId, "aCoordinate")
        }
    }

    private fun createShader(type:Int,code:String):Int{
        val shader=GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader,code)
        GLES30.glCompileShader(shader)
        var compiled=IntArray(1)
        GLES30.glGetShaderiv(shader,GLES30.GL_COMPILE_STATUS,compiled,0)
        if(compiled[0]==0){

            var length=IntArray(1)
            GLES30.glGetShaderiv(shader,GLES30.GL_INFO_LOG_LENGTH,length,0)
            if(length[0]>1){
                var log=GLES30.glGetShaderInfoLog(shader)
                Log.d(TAG,"Error Compiling shader type:$type")
                Log.d(TAG,"Error Compiling shader:$log")
            }
        }
        return shader
    }

    override fun draw(){
        Log.d(TAG,"draw mProgramId:$mProgramId")
        createGLPro()

        activeTexture()

        updateTexture()

        doDraw()
    }

    private fun doDraw() {
        // Set the viewport
//        GLES30.glViewport ( 0, 0, mWidth, mHeight );

        // Clear the color buffer

        GLES30.glUseProgram(mProgramId)
//        GLES30.glVertexAttrib1fv(1,colorBuffer)

//        GLES30.glEnableVertexAttribArray(textureHandle)
        GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,0,vertexBuffer)
        GLES30.glEnableVertexAttribArray(0)

//        GLES30.glVertexAttrib4f(0,1.0f,1.0f,0.0f,1.0f)

//        GLES30.glClear ( GLES30.GL_COLOR_BUFFER_BIT );
//        1.use layout identifier declare

        //设置顶点颜色  开始
        // 方式1.常量顶点属性设置
//        GLES30.glVertexAttrib4fv(1,colorBuffer)
        // 方式2.顶点数组
        GLES30.glVertexAttribPointer(1,4,GLES30.GL_FLOAT,true,0,colorBuffer)
//        2.bind uniform attribute index to field in shader.
        GLES30.glEnableVertexAttribArray(1)
        //设置顶点颜色  结束

//        GLES30.glVertexAttrib4f(vertexHandle)
//        GLES30.glVertexAttribPointer(textureHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)


//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glEnableVertexAttribArray(mCoordinateHandle)


        GLES30.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0)
        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(mCoordinateHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)


        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun activeTexture() {
        Log.d(VideoDrawer.TAG, "activeTexture $textureId")
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId)
        GLES30.glUniform1i(mTextureHandle,0)


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

    }

    private fun updateTexture() {
        surfaceTexture!!.updateTexImage()
    }

    override fun setTextureId(id:Int){
        textureId=id
        surfaceTexture = SurfaceTexture(textureId)
        if (callback != null) {
            callback!!.texture(surfaceTexture)
        }
    }

    var callback: TextureCallback? = null
        set(value) {
            field = value
            if (surfaceTexture != null && callback != null) {
                value?.texture(surfaceTexture)
            }
        }



    override fun translate(translateX:Float,translateY:Float){}
    override fun scale(scaleX:Float,scaleY:Float){}
    override fun setShader(shader: IGLShader){}
    override fun release(){
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
        GLES30.glDeleteTextures(1,intArrayOf(textureId),0)
        GLES30.glDeleteProgram(mProgramId)
    }

    override fun setVideoSize(w: Int, h: Int) {
        Log.d(VideoDrawer.TAG, "setVideoSize w:$w,h:$h")
        mVideoWidth = w
        mVideoHeight = h
    }

    override fun setSurfaceSize(w: Int, h: Int) {
        Log.d(VideoDrawer.TAG, "setSurfaceSize w:$w,h:$h")
        mSurfaceWidth = w
        mSurfaceHeight = h
    }

    init{
        createProgram()
    }
}
