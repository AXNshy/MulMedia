package com.luffyxu.mulmedia.gles3.square

import android.content.Context
import com.luffy.mulmedia.gles2.IGLShader
import com.luffy.mulmedia.utils.FileUtils

class SquareShader1(var context: Context? = null):IGLShader {
    override fun vertexShader(): String? {
        return FileUtils.getStringFromAssets(context, "gles3/vertex_element.glsl")
    }

    override fun fragmentShader(): String? {
        return FileUtils.getStringFromAssets(context, "gles3/frag_element.glsl")
    }
}