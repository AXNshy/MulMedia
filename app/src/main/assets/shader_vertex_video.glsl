precision mediump float;
attribute vec4 aPosition;
attribute vec2 aCoordinate;
uniform mat4 uMatrix;
varying vec2 vCoordinate;
void main(){
    gl_Position = uMatrix*aPosition;
    vCoordinate = aCoordinate;
}