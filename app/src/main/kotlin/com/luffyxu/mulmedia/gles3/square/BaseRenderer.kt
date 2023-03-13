package com.luffyxu.mulmedia.gles3.square

import android.opengl.GLES30
import com.luffy.mulmedia.gles2.IDrawer
import com.luffy.mulmedia.gles2.IGLShader
import com.luffyxu.mulmedia.gles3.createShader

abstract class BaseRenderer(val shader: IGLShader? = null) : IDrawer{


    var mProgramId : Int = -1
    var mVertexShader : Int = -1
    var mFragShader : Int = -1

   abstract fun onProgramLinked()
   abstract fun onDraw()

    override fun draw() {
        createProgram()
        if(mProgramId >= 0) {
            onDraw()
        }
    }

   fun createProgram(){
       if(mProgramId < 0){
           mProgramId = GLES30.glCreateProgram()

           mVertexShader = createShader(GLES30.GL_VERTEX_SHADER,shader?.vertexShader()!!)
           mFragShader = createShader(GLES30.GL_FRAGMENT_SHADER,shader?.fragmentShader()!!)

           GLES30.glAttachShader(mProgramId,mVertexShader)
           GLES30.glAttachShader(mProgramId,mFragShader)

           GLES30.glLinkProgram(mProgramId)

           onProgramLinked()
       }
   }

    override fun release() {
        GLES30.glDetachShader(mProgramId,mVertexShader)
        GLES30.glDetachShader(mProgramId,mFragShader)
        GLES30.glDeleteProgram(mProgramId)
    }
}