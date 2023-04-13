#version 300 es
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 a_texCoord;
out vec2 v_texCoord;
uniform mat4 u_MVPMatrix;
void main(){
    v_texCoord = a_texCoord;
    gl_Position = vPosition;
}

