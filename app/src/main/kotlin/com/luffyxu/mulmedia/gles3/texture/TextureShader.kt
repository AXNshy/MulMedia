package com.luffyxu.mulmedia.gles3.texture

import android.content.Context
import android.text.TextUtils
import com.luffy.mulmedia.gles2.IGLShader
import com.luffy.mulmedia.utils.FileUtils

class TextureShader(
    var context: Context? = null,
    val vertexShader: String? = null,
    val fragShader: String? = null
) : IGLShader {
    override fun vertexShader(): String {
        return if (TextUtils.isEmpty(vertexShader)) {
            FileUtils.getStringFromAssets(context, "gles3/shader_vertex_texture.glsl")
        } else vertexShader!!
    }

    override fun fragmentShader(): String {
        return if (TextUtils.isEmpty(fragShader)) {
            FileUtils.getStringFromAssets(context, "gles3/shader_frag_texture.glsl")
        } else fragShader!!
    }
}