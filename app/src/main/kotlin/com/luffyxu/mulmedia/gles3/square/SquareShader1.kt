package com.luffyxu.mulmedia.gles3.square

import android.content.Context
import com.luffyxu.opengles.base.shader.IGLShader
import com.luffyxu.opengles.base.utils.FileUtils

class SquareShader1(var context: Context) :
    IGLShader {
    override fun vertexShader(): String {
        return FileUtils.getStringFromAssets(context, "gles3/vertex_element.glsl")
    }

    override fun fragmentShader(): String {
        return FileUtils.getStringFromAssets(context, "gles3/frag_element.glsl")
    }
}