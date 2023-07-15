#version 300 es
#extension GL_OES_EGL_image_external : require
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;
in vec2 v_texCoord;
out vec4 fragColor;
uniform samplerExternalOES s_texture;
void main(){
    fragColor = texture(s_texture, v_texCoord);
}