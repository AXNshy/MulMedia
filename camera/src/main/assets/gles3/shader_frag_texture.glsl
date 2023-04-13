#version 300 es
precision mediump float;
in vec2 v_texCoord;
out vec4 fragColor;
uniform sampler2D s_textureY;
uniform sampler2D s_textureU;
uniform sampler2D s_textureV;
void main(){
    float y, u, v, r, g, b;
    y = texture(s_textureY, v_texCoord).r;
    u = texture(s_textureU, v_texCoord).r;
    v = texture(s_textureV, v_texCoord).r;
    u = u - 0.5;
    v = v - 0.5;
    r = y + 1.403 * v;
    g = y - 0.344 * u - 0.714 * v;
    b = y + 1.770 * u;
    fragColor = vec4(r, g, b, 1.0);
}