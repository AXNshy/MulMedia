package com.luffyxu.opengles.base.shader

import android.content.Context
import android.text.TextUtils
import com.luffyxu.opengles.base.utils.FileUtils

class NativeShader(
    var context: Context? = null,
    val vertexShader: String? = null,
    val fragShader: String? = null
) : IGLShader {
    override fun vertexShader(): String {
        return if (TextUtils.isEmpty(vertexShader)) {
            FileUtils.getStringFromAssets(context!!, "native_gles3/shader_vertex_texture.glsl")
        } else vertexShader!!
    }

    override fun fragmentShader(): String {
        return if (TextUtils.isEmpty(fragShader)) {
            FileUtils.getStringFromAssets(context!!, "native_gles3/shader_frag_texture.glsl")
        } else fragShader!!
    }
}