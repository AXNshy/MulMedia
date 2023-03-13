#version 300 es
#extension GL_OES_EGL_image_external : require
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;
uniform samplerExternalOES uTexture;
in vec2 vCoordinate;
out vec4 fragColor;
void main(){
    fragColor = texture(uTexture, vCoordinate);
}