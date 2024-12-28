package com.luffyxu.gles2;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.luffyxu.opengles.base.drawer.IDrawer;
import com.luffyxu.opengles.base.shader.IGLShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TriangleDrawer implements IDrawer {

    private final float[] vertexCoordinate = new float[]{
            -1f, -1f,
            1f, -1f,
            0f, 1f,
    };

    private final float[] textureCoordinate = new float[]{
            0f, 1f,
            1f, 1f,
            0.5f, 0f,
    };

    private int vertexHandle;

    private int textureHandle;

    private int mProgramId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;


    private int textureId;

    public TriangleDrawer() {
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

        vertexHandle = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        textureHandle = GLES20.glGetAttribLocation(mProgramId, "aCoordinate");

        GLES20.glUseProgram(mProgramId);
    }

    private static final String vertexShaderCode = "" +
            "attribute vec4 aPosition;" +
            "void main(){" +
            "gl_Position = aPosition;" +
            "}";


    private static final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "void main(){" +
            "   gl_FragColor = vec4(1.0,0.0,0.0,1.0);" +
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

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(textureHandle);

        GLES20.glVertexAttribPointer(vertexHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
    }

    @Override
    public void setTextureId(int[] id) {
        textureId = id[0];
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

    @Override
    public void release() {
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(textureHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteProgram(mProgramId);
    }

    @Override
    public void setSurfaceSize(int w, int h) {
        GLES30.glViewport(0,0,w,h);
    }

    @Override
    public void setVideoSize(int w, int h) {

    }
}
