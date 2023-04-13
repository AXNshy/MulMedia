package com.luffyxu.mulmedia.gles3

import android.opengl.GLES30
import android.util.Log
import com.luffyxu.opengles.base.egl.Transformation

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

fun resolveFlip(textureCoords: FloatArray, flip: Int) {
    when (flip) {
        Transformation.FLIP_HORIZONTAL -> {
            swapFloatArray(textureCoords, 0, 2)
            swapFloatArray(textureCoords, 4, 6)
        }
        Transformation.FLIP_VERTICAL -> {
            swapFloatArray(textureCoords, 1, 5)
            swapFloatArray(textureCoords, 3, 7)
        }
        Transformation.FLIP_HORIZONTAL_VERTICAL -> {
            swapFloatArray(textureCoords, 0, 2)
            swapFloatArray(textureCoords, 4, 6)
            swapFloatArray(textureCoords, 1, 5)
            swapFloatArray(textureCoords, 3, 7)
        }
        Transformation.FLIP_NONE -> {}
        else -> {}
    }
}

fun resolveRotate(textureCoords: FloatArray, rotation: Int) {
    val x: Float
    val y: Float
    when (rotation) {
        90 -> {
            x = textureCoords[0]
            y = textureCoords[1]
            textureCoords[0] = textureCoords[4]
            textureCoords[1] = textureCoords[5]
            textureCoords[4] = textureCoords[6]
            textureCoords[5] = textureCoords[7]
            textureCoords[6] = textureCoords[2]
            textureCoords[7] = textureCoords[3]
            textureCoords[2] = x
            textureCoords[3] = y
        }
        180 -> {
            swapFloatArray(textureCoords, 0, 6)
            swapFloatArray(textureCoords, 1, 7)
            swapFloatArray(textureCoords, 2, 4)
            swapFloatArray(textureCoords, 3, 5)
        }
        270 -> {
            x = textureCoords.get(0)
            y = textureCoords.get(1)
            textureCoords[0] = textureCoords.get(2)
            textureCoords[1] = textureCoords.get(3)
            textureCoords[2] = textureCoords.get(6)
            textureCoords[3] = textureCoords.get(7)
            textureCoords[6] = textureCoords.get(4)
            textureCoords[7] = textureCoords.get(5)
            textureCoords[4] = x
            textureCoords[5] = y
        }
        0 -> {}
        else -> {}
    }
}

fun swapFloatArray(array: FloatArray, i: Int, j: Int) {
    val x = array[i]
    array[i] = array[j]
    array[j] = x
}