package com.luffyxu.mulmedia.gles3.triangle

import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleDrawer : com.luffyxu.opengles.base.egl.IDrawer {

    val TAG = "GLRenderer"
    private val vertexCoordinate = floatArrayOf(
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        0f, 1f, 0.0f,
    )

    private val color = floatArrayOf(
        1f, 0f, 0f, 1f, 1f, 1f
    )


    private var mProgramId = -1
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null

    private var colorHandle = -1;
    private var positionHandle = -1;

    private fun createProgram() {
        Log.d(TAG, "createProgram")
        val buffer = ByteBuffer.allocateDirect(vertexCoordinate.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer?.put(vertexCoordinate)
        vertexBuffer?.position(0)

        val buffer2 = ByteBuffer.allocateDirect(color.size * 4)
        buffer2.order(ByteOrder.nativeOrder())
        colorBuffer = buffer2.asFloatBuffer().apply {
            put(color)
            position(0)
        }
    }

    private fun createGLPro() {
        if (mProgramId == -1) {
            Log.d(TAG, "createGLPro")
            val vertexShader =
                createShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode3)
            val fragShader =
                createShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgramId = GLES30.glCreateProgram()

            GLES30.glBindAttribLocation(mProgramId,colorHandle,"a_color")
            GLES30.glBindAttribLocation(mProgramId,positionHandle,"vPosition")


            GLES30.glAttachShader(mProgramId, vertexShader)
            GLES30.glAttachShader(mProgramId, fragShader)
            GLES30.glLinkProgram(mProgramId)
        }
    }

    private fun createShader(type: Int, code: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, code)
        GLES30.glCompileShader(shader)
        var compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {

            var length = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, length, 0)
            if (length[0] > 1) {
                var log = GLES30.glGetShaderInfoLog(shader)
                Log.d(TAG, "Error Compiling shader type:$type")
                Log.d(TAG, "Error Compiling shader:$log")
            }
        }
        return shader
    }

    override fun draw() {
        Log.d(TAG, "draw mProgramId:$mProgramId")
        createGLPro()
        // Set the viewport
//        GLES30.glViewport ( 0, 0, mWidth, mHeight );

        // Clear the color buffer

        GLES30.glUseProgram(mProgramId)
//        GLES30.glVertexAttrib1fv(1,colorBuffer)

//        GLES30.glEnableVertexAttribArray(textureHandle)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(0)

//        GLES30.glVertexAttrib4f(0,1.0f,1.0f,0.0f,1.0f)

//        GLES30.glClear ( GLES30.GL_COLOR_BUFFER_BIT );
//        1.use layout identifier declare

        //设置顶点颜色  开始
        // 方式1.常量顶点属性设置
//        GLES30.glVertexAttrib4fv(1,colorBuffer)
        // 方式2.顶点数组
        GLES30.glVertexAttribPointer(1,3,GLES30.GL_FLOAT, true,0,colorBuffer)
//        2.bind uniform attribute index to field in shader.
        GLES30.glEnableVertexAttribArray(1)
        //设置顶点颜色  结束

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }


    override fun setTextureId(id: IntArray) {}

    override fun translate(translateX: Float, translateY: Float) {}
    override fun scale(scaleX: Float, scaleY: Float) {}
    override fun setShader(shader: com.luffyxu.opengles.base.egl.IGLShader) {}
    override fun release() {
    }

    override fun setSurfaceSize(w: Int, h: Int) {
        GLES30.glViewport(0, 0, w, h)
    }

    override fun setVideoSize(w: Int, h: Int) {}

    companion object {
        //1.use layout identifier declare
        //使用常量顶点颜色
        private const val vertexShaderCode1 = """#version 300 es
            layout(location = 0) in vec4 vPosition;
            layout(location = 1) in vec4 a_color;
            out vec4 v_color;
            void main(){
                v_color = a_color;
                gl_Position = vPosition;
            }
        """

        //使用顶点颜色数组
        private const val vertexShaderCode3 = """#version 300 es
            layout(location = 0) in vec4 vPosition;
            layout(location = 1) in vec2 a_color;
            out vec4 v_color;
            void main(){
                v_color = vec4(a_color,0,1);
                gl_Position = vPosition;
            }
        """

        private const val vertexShaderCode2 = """#version 300 es
            in vec4 vPosition;
            in vec4 a_color;
            out vec4 v_color;
            void main(){
                v_color = vec4(a_color);
                gl_Position = vPosition;
            }
        """

        private const val fragmentShaderCode = """#version 300 es
            precision mediump float;
            in vec4 v_color;
            out vec4 fragColor;
            void main(){
               fragColor = v_color;
            }
        """

    }

    init {
        createProgram()
    }
}

