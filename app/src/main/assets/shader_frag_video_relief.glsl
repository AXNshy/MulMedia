#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTexture;
varying vec2 vCoordinate;
uniform vec2 TexSize;
void main()
{
    vec2 tex =vCoordinate;
    vec2 upLeftUV = vec2(tex.x-1.0/TexSize.x, tex.y-1.0/TexSize.y);
    vec4 curColor = texture2D(uTexture, vCoordinate);
    vec4 upLeftColor = texture2D(uTexture, upLeftUV);
    vec4 delColor = curColor - upLeftColor;
    float h = 0.3*delColor.x + 0.59*delColor.y + 0.11*delColor.z;
    vec4 bkColor = vec4(0.5, 0.5, 0.5, 1.0);
    gl_FragColor = vec4(h, h, h, 0.0) +bkColor;
}
//vec2 mosaicSize = vec2(8, 8);
//void main()
//{
//    vec2 intXY = vec2(vCoordinate.x*TexSize.x, vCoordinate.y*TexSize.y);
//    vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x)*mosaicSize.x, floor(intXY.y/mosaicSize.y)*mosaicSize.y);
//    vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);
//    vec4 baseMap = texture2D(uTexture, UVMosaic);
//    gl_FragColor = baseMap;
//}
