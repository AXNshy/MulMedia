package com.luffyxu.opengles.base.shader

import com.luffyxu.base.utils.ContextUtils
import com.luffyxu.base.utils.FileUtils

class AssetShader(
    val vertexShaderPath: String? = null,
    val fragShaderPath: String? = null,
) : IGLShader {
    override fun vertexShader(): String {
        return FileUtils.getStringFromAssets(ContextUtils.context!!, vertexShaderPath)
    }

    override fun fragmentShader(): String {
        return FileUtils.getStringFromAssets(ContextUtils.context!!, fragShaderPath)
    }
}