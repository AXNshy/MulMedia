package com.luffyxu.mulmedia.gles3

import android.opengl.GLES30
import android.util.Log

val TAG = "Utils"


fun createShader(type:Int,code:String):Int{
    val shader= GLES30.glCreateShader(type)
    GLES30.glShaderSource(shader,code)
    GLES30.glCompileShader(shader)
    var compiled=IntArray(1)
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS,compiled,0)
    if(compiled[0]==0){

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

fun checkGLError(method: String) {
    var err: Int = GLES30.glGetError()
    if (err != GLES30.GL_NO_ERROR) {
        println("method($method) error($err)")
    }
}