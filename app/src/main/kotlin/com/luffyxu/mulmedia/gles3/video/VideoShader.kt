package com.luffyxu.mulmedia.gles3.video

import android.content.Context
import android.text.TextUtils
import com.luffyxu.opengles.base.shader.IGLShader
import com.luffyxu.opengles.base.utils.FileUtils

class VideoShader(
    var context: Context,
    val vertexShader: String? = null,
    val fragShader: String? = null
) : IGLShader {
    override fun vertexShader(): String {
        return if (TextUtils.isEmpty(vertexShader)) {
            FileUtils.getStringFromAssets(context, "gles3/shader_vertex_video.glsl")
        } else vertexShader!!
    }

    override fun fragmentShader(): String {
        return if (TextUtils.isEmpty(fragShader)) {
            FileUtils.getStringFromAssets(context, "gles3/shader_frag_video.glsl")
        } else fragShader!!
    }
}