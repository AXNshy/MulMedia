package com.luffyxu.mulmedia.gles3.square

import android.content.Context
import com.luffyxu.opengles.base.egl.FileUtils

class SquareShader1(var context: Context) :
    com.luffyxu.opengles.base.egl.IGLShader {
    override fun vertexShader(): String {
        return FileUtils.getStringFromAssets(context, "gles3/vertex_element.glsl")
    }

    override fun fragmentShader(): String {
        return FileUtils.getStringFromAssets(context, "gles3/frag_element.glsl")
    }
}