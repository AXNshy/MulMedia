package com.luffyxu.mulmedia.gles3.square

import android.opengl.GLES30
import com.luffyxu.opengles.base.egl.IGLShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Square2Renderer(shader: com.luffyxu.opengles.base.egl.IGLShader? = null) :
    BaseRenderer(shader) {
    val VERTEX_POS_SIZE = 3
    val VERTEX_COLOR_SIZE = 2
    val VERTEX_ATTR_SIZE = VERTEX_COLOR_SIZE + VERTEX_POS_SIZE

    //数组结构
    val vertexBPos: FloatArray = floatArrayOf()
    val vertexBColor: FloatArray = floatArrayOf()

    var vertexBPosBuffer: FloatBuffer
    var vertexBColorBuffer: FloatBuffer
    var vertexBPosIndex = 2
    var vertexBColorIndex = 3
    //数组结构


    init {
        vertexBPosBuffer = ByteBuffer.allocate(vertexBPos.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(vertexBPos).position(0)
        }

        vertexBColorBuffer = ByteBuffer.allocate(vertexBColor.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(vertexBColor).position(0)
        }
    }

    override fun onProgramLinked() {

    }

    override fun onDraw() {
        GLES30.glUseProgram(mProgramId)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)


        GLES30.glVertexAttribPointer(vertexBPosIndex,VERTEX_POS_SIZE,GLES30.GL_FLOAT,false,0,vertexBPosBuffer)
        GLES30.glVertexAttribPointer(vertexBColorIndex,VERTEX_COLOR_SIZE,GLES30.GL_FLOAT,false,0,vertexBColorBuffer)


        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3)


        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    override fun setTextureId(id: IntArray) {
    }


    override fun setSurfaceSize(w: Int, h: Int) {
        TODO("Not yet implemented")
    }

    override fun setVideoSize(w: Int, h: Int) {
        TODO("Not yet implemented")
    }

    override fun translate(translateX: Float, translateY: Float) {
        TODO("Not yet implemented")
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        TODO("Not yet implemented")
    }

    override fun setShader(shader: IGLShader) {
    }
}