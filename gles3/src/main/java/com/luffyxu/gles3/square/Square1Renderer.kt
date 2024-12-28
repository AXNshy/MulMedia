package com.luffyxu.gles3.square

import android.opengl.GLES30
import com.luffyxu.opengles.base.shader.IGLShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Square1Renderer(shader: IGLShader? = null) :
    BaseRenderer(shader) {
    val VERTEX_POS_SIZE = 3
    val VERTEX_COLOR_SIZE = 2
    val VERTEX_ATTR_SIZE = VERTEX_COLOR_SIZE + VERTEX_POS_SIZE

    //结构数组
    val vertexAAttrs: FloatArray = floatArrayOf(
        -1f, 1f, 0f, 1f, 0f,
        1f, 1f, 0f, 0f, 1f,
        0f, -1f, 0f, 1f, 1f

    )
    lateinit var vertexAAttrsBuffer : FloatBuffer

    var vertexAPosIndex = 0
    var vertexAColorIndex = 1
    //结构数组


    init {

    }

    override fun onProgramLinked() {
//        vertexAAttrsBuffer = ByteBuffer.allocate(vertexAAttrs.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
//            put(vertexAAttrs).position(0)
//        }

        val buffer = ByteBuffer.allocateDirect(vertexAAttrs.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexAAttrsBuffer = buffer.asFloatBuffer()
        vertexAAttrsBuffer.put(vertexAAttrs)
        vertexAAttrsBuffer.position(0)
    }

    override fun onDraw() {
        GLES30.glUseProgram(mProgramId)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(vertexAPosIndex,VERTEX_POS_SIZE,GLES30.GL_FLOAT,false,VERTEX_ATTR_SIZE * 4,vertexAAttrsBuffer)
        GLES30.glVertexAttribPointer(vertexAColorIndex,VERTEX_COLOR_SIZE,GLES30.GL_FLOAT,false,VERTEX_ATTR_SIZE * 4,vertexAAttrsBuffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    override fun setTextureId(id: IntArray) {
    }


    override fun setSurfaceSize(w: Int, h: Int) {
    }

    override fun setVideoSize(w: Int, h: Int) {
    }

    override fun translate(translateX: Float, translateY: Float) {
    }

    override fun scale(scaleX: Float, scaleY: Float) {
    }

    override fun setShader(shader: IGLShader) {
    }
}