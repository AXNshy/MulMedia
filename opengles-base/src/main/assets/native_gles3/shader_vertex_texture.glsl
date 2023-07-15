#version 300 es
precision mediump float;
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 a_texCoord;
uniform mat4 u_MVPMatrix;
out vec2 v_texCoord;
void main(){
    gl_Position = vPosition;
    v_texCoord = a_texCoord;
}