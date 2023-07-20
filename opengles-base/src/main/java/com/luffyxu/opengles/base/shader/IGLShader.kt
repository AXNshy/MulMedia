package com.luffyxu.opengles.base.shader

interface IGLShader {
    fun vertexShader(): String
    fun fragmentShader(): String
}