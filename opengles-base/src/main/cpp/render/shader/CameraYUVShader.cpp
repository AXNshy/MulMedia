//
// Created by 徐振乾 on 2023/7/2.
//

#include "CameraYUVShader.h"

char *CameraYUVShader::vertexShader() {
    char str[] = "#version 300 es\n"
                 "layout(location = 0) in vec4 vPosition;\n"
                 "layout(location = 1) in vec2 a_texCoord;\n"
                 "out vec2 v_texCoord;\n"
                 "uniform mat4 u_MVPMatrix;\n"
                 "void main(){\n"
                 "    v_texCoord = a_texCoord;\n"
                 "    gl_Position = vPosition;\n"
                 "}";
    return str;
}

char *CameraYUVShader::fragmentShader() {
    char str[] = "#extension GL_OES_EGL_image_external : require\n"
                 "#version 300 es\n"
                 "precision mediump float;\n"
                 "in vec2 v_texCoord;\n"
                 "out vec4 fragColor;\n"
                 "uniform samplerExternalOES s_texture;\n"
                 "void main(){\n"
                 "    fragColor = texture(s_texture, v_texCoord);"
                 "}";
    return str;
}
