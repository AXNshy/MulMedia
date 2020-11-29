#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTexture;
varying vec2 vCoordinate;
void main(){
    vec4 color = texture2D(uTexture, vCoordinate);
    gl_FragColor = color;
}