#version 300 es
#extension GL_OES_EGL_image_external : require
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;
in vec2 v_texCoord;
out vec4 fragColor;
uniform samplerExternalOES s_texture;

uniform float time;

#define _SnowflakeAmount 200
#define _BlizardFactor 0.2

float rnd(float x) {
    return fract(sin(dot(vec2(x+47.49, 38.2467/(x+2.3)), vec2(12.9898, 78.233)))* (43758.5453));
}

float drawCircle(vec2 uv, vec2 center, float radius) {
    return 1.0 - smoothstep(0.0, radius, length(uv - center));
}

void main(){
    fragColor = texture(s_texture, v_texCoord);

    float j;
    for (int i=0; i<_SnowflakeAmount; i++) {
        j = float(i);
        float speed = 0.3+rnd(cos(j))*(0.7+0.5*cos(j/(float(_SnowflakeAmount)*0.25)));
        vec2 center = vec2((0.25-v_texCoord.y)*_BlizardFactor+rnd(j)+0.1*cos(time+sin(j)), mod(sin(j)-speed*(time*1.5*(0.1+_BlizardFactor)), 1.0));
        fragColor += vec4(0.9*drawCircle(v_texCoord, center, 0.001+speed*0.012));
    }
}