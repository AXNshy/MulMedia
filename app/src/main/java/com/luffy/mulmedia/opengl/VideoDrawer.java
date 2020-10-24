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

    private int mMVPMatrix;

    private int textureHandle;

    private int mProgramId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private float[] mMatrix;
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];

    private float[] sizeRatio = new float[2];

    private boolean istra = false;

    private int textureId;

    private SurfaceTexture surfaceTexture;


    private int mSurfaceWidth = 1;
    private int mSurfaceHeight = 1;

    private int mVideoWidth = 1;
    private int mVideoHeight = 1;


    private int mTranslateX = 0;
    private int mTranslateY = 0;

    public VideoDrawer() {
        createProgram();
    }

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
        mMVPMatrix = GLES20.glGetUniformLocation(mProgramId, "uMatrix");
        GLES20.glUseProgram(mProgramId);
    }

    private String getVertexShaderCode() {
        return "attribute vec4 aPosition;" +
                "precision mediump float;" +
                "attribute vec2 aCoordinate;" +
                "uniform mat4 uMatrix;" +
                "varying vec2 vCoordinate;" +
                "void main(){" +
                "gl_Position = uMatrix*aPosition;" +
                "vCoordinate = aCoordinate;" +
                "}";

    }

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
    public void translate(float translateX, float translateY) {
        if (mMatrix != null) {
            transformTranslationMatrix(translateX, translateY);
        }
    }

    @Override
    public void scale(float scaleX, float scaleY) {
        transformScaleMatrix(scaleX, scaleY);
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
//        transformScaleMatrix(1f, 1f);
        initialMatrix();

//        transformScaleMatrix(2f, 2f);/**/
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
        {
//        createGLPro();
        }
//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnableVertexAttribArray(vertexPosHandle);
        GLES20.glEnableVertexAttribArray(texturePosHandle);


        GLES20.glUniformMatrix4fv(mMVPMatrix, 1, false, mMatrix, 0);
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

    @Override
    public void release() {
        GLES20.glDisableVertexAttribArray(vertexPosHandle);
        GLES20.glDisableVertexAttribArray(texturePosHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[1], 0);
        GLES20.glDeleteProgram(mProgramId);
    }


    private void initialMatrix() {
        if (mMatrix != null) {
            return;
        }
        mMatrix = new float[16];
        float top = 1f, bottom = -1f;
        float verScale = (float) mSurfaceHeight / (float) mVideoHeight;
        float horScale = (float) mSurfaceWidth / (float) mVideoWidth;
        if (horScale < verScale) {
            sizeRatio[1] = top = verScale / horScale * 2;
            bottom = -top;
        }
        sizeRatio[0] = 2;
        Matrix.orthoM(projectionMatrix, 0, -2, 2, bottom, top, 3, 5);
//        Matrix.orthoM(projectionMatrix, 0, -2, 2, bottom, top, 1, 2);

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Log.v(TAG, "initialMatrix " + Arrays.toString(mMatrix));
    }


    private void transformTranslationMatrix(float translationX, float translationY) {
        float dx = translationX / (float) mSurfaceWidth;
        float dy = translationY / (float) mSurfaceHeight;
        Log.v(TAG, "transformTranslationMatrix  dx dy:" + dx + ", " + dy);
        Matrix.translateM(mMatrix, 0, sizeRatio[0] * dx * 2f, -sizeRatio[1] * dy * 2f, 0);
        Log.v(TAG, "transformTranslationMatrix :" + Arrays.toString(mMatrix));
        Log.v(TAG, "sizeRatio :" + Arrays.toString(sizeRatio));
    }

    private void transformScaleMatrix(float scaleX, float scaleY) {
        Matrix.scaleM(mMatrix, 0, -scaleX, scaleX, 0);
//        Log.d(TAG,"videoSizeChangeMatrix " + Arrays.toString(videoSizeChangeMatrix));
        Log.v(TAG, "transformScaleMatrix :" + Arrays.toString(mMatrix));
    }
}
