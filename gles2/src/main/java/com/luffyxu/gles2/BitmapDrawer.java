package com.luffyxu.gles2;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.luffyxu.opengles.base.drawer.IDrawer;
import com.luffyxu.opengles.base.shader.IGLShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BitmapDrawer implements IDrawer {
    private final float[] vertexCoordinate = new float[]{
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
    };

    private final float[] textureCoordinate = new float[]{
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };

    private int vertexPosHandle;

    private int texturePosHandle;

    private int textureHandle;

    private int mProgramId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;


    private int textureId;

    private final Bitmap mBitmap;

    public BitmapDrawer(Bitmap bitmap) {
        mBitmap = bitmap;
        createProgram();
    }

    private void createProgram() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoordinate.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoordinate);
        vertexBuffer.position(0);


        ByteBuffer buffer1 = ByteBuffer.allocateDirect(textureCoordinate.length * 4);
        buffer1.order(ByteOrder.nativeOrder());
        textureBuffer = buffer1.asFloatBuffer();
        textureBuffer.put(textureCoordinate);
        textureBuffer.position(0);

//        createGLPro();
    }

    private void createGLPro() {
        int vertexShader = createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramId, vertexShader);
        GLES20.glAttachShader(mProgramId, fragShader);
        GLES20.glLinkProgram(mProgramId);

        vertexPosHandle = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        texturePosHandle = GLES20.glGetAttribLocation(mProgramId, "aCoordinate");
        textureHandle = GLES20.glGetUniformLocation(mProgramId, "uTexture");
        GLES20.glUseProgram(mProgramId);
    }

    private static final String vertexShaderCode = "" +
            "attribute vec4 aPosition;" +
            "attribute vec2 aCoordinate;" +
            "varying vec2 vCoordinate;" +
            "void main(){" +
            "gl_Position = aPosition;" +
            "vCoordinate = aCoordinate;" +
            "}";

//    private String getVertexShader(){
//        return  //顶点坐标
//                "attribute vec2 aPosition;" +
//                        //纹理坐标
//                        "attribute vec2 aCoordinate;" +
//                        //用于传递纹理坐标给片元着色器，命名和片元着色器中的一致
//                        "varying vec2 vCoordinate;" +
//                        "void main() {" +
//                        "  gl_Position = aPosition;" +
//                        "  vCoordinate = aCoordinate;" +
//                        "}";
//    }


    private static final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 vCoordinate;" +
            "void main(){" +
            "vec4 color = texture2D(uTexture,vCoordinate);" +
            "gl_FragColor = color;" +
            "}";

    private int createShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }


    @Override
    public void draw() {

        createGLPro();

        activeTexture();

        bindTextureToBitmap();

        doDraw();
    }

    private void bindTextureToBitmap() {
        if (!mBitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        }
    }

    private void activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureHandle, 0);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }


    private void doDraw() {
        GLES20.glEnableVertexAttribArray(vertexPosHandle);
        GLES20.glEnableVertexAttribArray(texturePosHandle);

        GLES20.glVertexAttribPointer(vertexPosHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(texturePosHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void setTextureId(int[] id) {
        textureId = id[0];
    }

    @Override
    public void release() {


        GLES20.glDisableVertexAttribArray(vertexPosHandle);
        GLES20.glDisableVertexAttribArray(texturePosHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[1], 0);
        GLES20.glDeleteProgram(mProgramId);
    }

    @Override
    public void setSurfaceSize(int w, int h) {

    }

    @Override
    public void setVideoSize(int w, int h) {

    }

    @Override
    public void translate(float translateX, float translateY) {

    }

    @Override
    public void scale(float scaleX, float scaleY) {

    }

    @Override
    public void setShader(IGLShader shader) {

    }
}
