package com.luffy.mulmedia.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class VideoDrawer implements IDrawer {
    public static final String TAG = "VideoDrawer";
    private float[] vertexCoordinate = new float[]{
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
    };

    private float[] textureCoordinate = new float[]{
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };

    private int vertexPosHandle;

    private int texturePosHandle;

    private int sizeChangeMatrixHandle;

    private int textureHandle;

    private int mProgramId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private float[] videoSizeChangeMatrix = new float[16];

    private void createMatrix() {

        float top = 1f, bottom = -1f;
        float verScale = (float) mSurfaceHeight / (float) mVideoHeight;
        float horScale = (float) mSurfaceWidth / (float) mVideoWidth;
        if (mVideoHeight / mVideoWidth < mSurfaceHeight / mSurfaceWidth) {
            top = verScale / horScale;
            bottom = -top;
        }
        Matrix.orthoM(videoSizeChangeMatrix, 0, -1, 1, bottom, top, 1, 2);
//        Log.d(TAG,"videoSizeChangeMatrix " + Arrays.toString(videoSizeChangeMatrix));
        Log.v(TAG, "bottom :" + bottom + ",top :" + top);
    }

    private int textureId;

    private SurfaceTexture surfaceTexture;

    public VideoDrawer() {
        createProgram();
    }

    private int mSurfaceWidth = 1;
    private int mSurfaceHeight = 1;

    private int mVideoWidth = 1;
    private int mVideoHeight = 1;

    @Override
    public void setVideoSize(int w, int h) {
        Log.d(TAG, "setVideoSize w:" + w + ",h:" + h);
        mVideoWidth = w;
        mVideoHeight = h;
    }

    @Override
    public void setSurfaceSize(int w, int h) {
        Log.d(TAG, "setSurfaceSize w:" + w + ",h:" + h);
        mSurfaceWidth = w;
        mSurfaceHeight = h;
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
        int vertexShader = createShader(GLES20.GL_VERTEX_SHADER, getVertexShaderCode());
        int fragShader = createShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());

        mProgramId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramId, vertexShader);
        GLES20.glAttachShader(mProgramId, fragShader);
        GLES20.glLinkProgram(mProgramId);

        vertexPosHandle = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        texturePosHandle = GLES20.glGetAttribLocation(mProgramId, "aCoordinate");
        textureHandle = GLES20.glGetUniformLocation(mProgramId, "uTexture");
        sizeChangeMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uMatrix");
        GLES20.glUseProgram(mProgramId);
    }

    private String getVertexShaderCode() {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "uniform mat4 uMatrix;" +
                "varying vec2 vCoordinate;" +
                "void main(){" +
                "gl_Position = aPosition*uMatrix;" +
                "vCoordinate = aCoordinate;" +
                "}";

    }
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


    private String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "uniform samplerExternalOES uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main(){" +
                "vec4 color = texture2D(uTexture,vCoordinate);" +
                "gl_FragColor = color;" +
                "}";
    }

    private int createShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }


    @Override
    public void draw() {

        initDefMatrix();

        createGLPro();

        activeTexture();

        updateTexture();

        doDraw();
    }

    private void initDefMatrix() {
        createMatrix();
    }

    private void updateTexture() {
        surfaceTexture.updateTexImage();
    }

    private void activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureHandle, 0);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }


    private void doDraw() {
        GLES20.glEnableVertexAttribArray(vertexPosHandle);
        GLES20.glEnableVertexAttribArray(texturePosHandle);


        GLES20.glUniformMatrix4fv(sizeChangeMatrixHandle, 1, false, videoSizeChangeMatrix, 0);
        GLES20.glVertexAttribPointer(vertexPosHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(texturePosHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void setTextureId(int id) {
        textureId = id;
        surfaceTexture = new SurfaceTexture(textureId);
        if (callback != null) {
            callback.texture(surfaceTexture);
        }
    }

    private TextureCallback callback;


    public void setCallback(TextureCallback callback) {
        this.callback = callback;
        if (surfaceTexture != null && callback != null) {
            callback.texture(surfaceTexture);
        }
    }

    public interface TextureCallback {
        void texture(SurfaceTexture surface);
    }

    @Override
    public void release() {


        GLES20.glDisableVertexAttribArray(vertexPosHandle);
        GLES20.glDisableVertexAttribArray(texturePosHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[1], 0);
        GLES20.glDeleteProgram(mProgramId);
    }
}
