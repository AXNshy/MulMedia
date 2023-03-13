#version 300 es
precision mediump float;
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aCoordinate;
out vec4 v_color;
uniform mat4 uMatrix;
out vec2 vCoordinate;
void main(){
    gl_Position = uMatrix*aPosition;
    vCoordinate = aCoordinate;
}