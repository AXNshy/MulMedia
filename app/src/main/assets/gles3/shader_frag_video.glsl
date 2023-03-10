#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTexture;
in vec2 vCoordinate;
layout(location = 0) out vec4 outColor;
void main(){
    outColor = texture(uTexture, vCoordinate);
}