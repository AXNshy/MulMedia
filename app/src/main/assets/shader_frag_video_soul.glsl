#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vCoordinate;
varying float inAlpha;
uniform samplerExternalOES uTexture;
uniform float progress;
uniform int drawFbo;
uniform sampler2D uSoulTexture;
void main() {
    // 透明度[0,0.4]
    float alpha = 0.6 * (1.0 - progress);
    // 缩放比例[1.0,1.8]
    float scale = 1.0 + (1.5 - 1.0) * progress;

    // 放大纹理坐标
    // 根据放大比例，得到放大纹理坐标 [0,0],[0,1],[1,1],[1,0]
    float soulX = 0.5 + (vCoordinate.x - 0.5) / scale;
    float soulY = 0.5 + (vCoordinate.y - 0.5) / scale;
    vec2 soulTextureCoords = vec2(soulX, soulY);
    // 获取对应放大纹理坐标下的纹素(颜色值rgba)
    vec4 soulMask = texture2D(uSoulTexture, soulTextureCoords);

    vec4 color = texture2D(uTexture, vCoordinate);

    if (drawFbo == 0) {
        // 颜色混合 默认颜色混合方程式 = mask * (1.0-alpha) + weakMask * alpha
        //                "    gl_FragColor = color * (1.0 - alpha);" +
        //                "    gl_FragColor = soulMask * alpha;" +
        gl_FragColor = color * (1.0 - alpha) + soulMask * alpha;
    } else {
        gl_FragColor = vec4(color.r, color.g, color.b, inAlpha);
    }
}