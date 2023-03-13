#version 300 es
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 a_color;
out vec4 v_color;
void main(){
    v_color = vec4(a_color, 0, 1);
    gl_Position = vPosition;
}

