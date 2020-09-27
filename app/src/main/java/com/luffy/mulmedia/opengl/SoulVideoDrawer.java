package com.luffy.mulmedia.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.luffy.mulmedia.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class SoulVideoDrawer implements IDrawer {
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

    private int mVertexPosHandler = -1;

    private int mCoordinateHandler = -1;

    private int mAlphaHandler = -1;

    private int mMVPMatrixHandler = -1;

    private int textureHandler = -1;

    private int mProgramId = -1;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private float[] mMatrix;
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];

    private float[] sizeRatio = new float[2];

    private int textureId = -1;

    private SurfaceTexture surfaceTexture;


    private int mSurfaceWidth = 1;
    private int mSurfaceHeight = 1;

    private int mVideoWidth = 1;
    private int mVideoHeight = 1;


    private float[] mReverseVertexCoors = new float[]{
            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f,
    };

    private float[] mDefaultVertexCoors = new float[]{
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
    };

    private float[] mSoulVertexCoors = mReverseVertexCoors;
    private int mSoulFrameBuffer = -1;
    private int mSoulTextureId = -1;
    private int mSoulTextureHandler;
    private int mProgressHandler = -1;
    private int mDrawFBO = 1;
    private int mDrawFBOHandler = -1;
    private long mModifyTime = -1;
    private float mAlpha = 1f;

    public SoulVideoDrawer() {
        initPos();
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

    private void initPos() {
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

    private String getVertexShaderCode() {
        return "attribute vec4 aPosition;" +
                "precision mediump float;" +
                "uniform mat4 uMatrix;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "attribute float alpha;" +
                "varying float inAlpha;" +
                "void main(){" +
                "    gl_Position = uMatrix*aPosition;" +
                "    vCoordinate = aCoordinate;" +
                "    inAlpha = alpha;" +
                "}";

    }

    private String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "varying float inAlpha;" +
                "uniform samplerExternalOES uTexture;" +
                "uniform float progress;" +
                "uniform int drawFbo;" +
                "uniform sampler2D uSoulTexture;" +
                "void main(){" +
                "    float alpha = 0.6 * (1.0-progress);" +
                "    float scale = 1.0 + (1.5 - 1.0) * progress;" +
                "    float soulX = 0.5 + (vCoordinate.x - 0.5) / scale;" +
                "    float soulY = 0.5 + (vCoordinate.y - 0.5) / scale;" +
                "    vec2 soulTextureCoors = vec2(soulX,soulY);" +
                "    vec4 soulMask = texture2D(uSoulTexture,soulTextureCoors);" +
                "    vec4 color = texture2D(uTexture,vCoordinate);" +
                "    if(drawFbo == 0){" +
                "        gl_FragColor = color * (1.0-alpha) + soulMask * alpha;" +
                "    }else{" +
                "        gl_FragColor = vec4(color.r,color.g,color.b,inAlpha);" +
                "    }" +
                "}";
    }

    private int createShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }


    private void initDefMatrix() {
        initialMatrix();
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
        if (textureId != -1) {
            initDefMatrix();

            createGLPro();

            updateFBO();
//
            activeSoulTexture();

            activeDefTexture();

            updateTexture();

            doDraw();
        }
    }

    private void createGLPro() {
        if (mProgramId == -1) {
            int vertexShader = createShader(GLES20.GL_VERTEX_SHADER, getVertexShaderCode());
            int fragShader = createShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());

            mProgramId = GLES20.glCreateProgram();
            GLES20.glAttachShader(mProgramId, vertexShader);
            GLES20.glAttachShader(mProgramId, fragShader);
            GLES20.glLinkProgram(mProgramId);

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgramId, "aPosition");
            mCoordinateHandler = GLES20.glGetAttribLocation(mProgramId, "aCoordinate");
            mAlphaHandler = GLES20.glGetAttribLocation(mProgramId, "alpha");
            textureHandler = GLES20.glGetUniformLocation(mProgramId, "uTexture");
            mMVPMatrixHandler = GLES20.glGetUniformLocation(mProgramId, "uMatrix");
            mSoulTextureHandler = GLES20.glGetUniformLocation(mProgramId, "uSoulTexture");
            mProgressHandler = GLES20.glGetUniformLocation(mProgramId, "progress");
            mDrawFBOHandler = GLES20.glGetUniformLocation(mProgramId, "drawFbo");
        }
        GLES20.glUseProgram(mProgramId);
    }

    private void updateFBO() {
        if (mSoulTextureId == -1) {
            mSoulTextureId = OpenGLUtils.createFBOTexture(mVideoWidth, mVideoHeight);
        }

        if (mSoulFrameBuffer == -1) {
            mSoulFrameBuffer = OpenGLUtils.createFrameBuffer();
        }

        if (System.currentTimeMillis() - mModifyTime > 500) {
            mModifyTime = System.currentTimeMillis();
            OpenGLUtils.bindFBO(mSoulFrameBuffer, mSoulTextureId);
            configFBOViewport();

            activeDefTexture();

            updateTexture();

            doDraw();

            OpenGLUtils.unbindFBO();

            configDefViewport();
        }

    }

    private void configFBOViewport() {
        mDrawFBO = 1;
        Matrix.setIdentityM(mMatrix, 0);
        vertexCoordinate = mReverseVertexCoors;
        initPos();
        GLES20.glViewport(0, 0, mVideoWidth, mVideoHeight);
        GLES20.glClearColor(0.0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void configDefViewport() {
        mDrawFBO = 0;
        mMatrix = null;
        vertexCoordinate = mDefaultVertexCoors;
        initPos();
        initDefMatrix();
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
    }

    private void activeSoulTexture() {
        activeTexture(GLES11.GL_TEXTURE_2D, mSoulTextureId, 1, mSoulTextureHandler);
    }

    private void activeDefTexture() {
        activeTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId, 0, textureHandler);
    }

    private void activeTexture(int type, int textureId, int index, int textureHandler) {
        Log.d(TAG, "activeTexture type:" + type + ",textureId:" + textureId + ",index:" + index + ",textureHandler:" + textureHandler);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(type, textureId);
        GLES20.glUniform1i(textureHandler, index);

        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    private void updateTexture() {
        if (surfaceTexture != null)
            surfaceTexture.updateTexImage();
    }

    private void doDraw() {

//        GLES20.glClearColor(0f, 0f, 0f, 0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnableVertexAttribArray(mVertexPosHandler);
        GLES20.glEnableVertexAttribArray(mCoordinateHandler);


        GLES20.glUniformMatrix4fv(mMVPMatrixHandler, 1, false, mMatrix, 0);
        GLES20.glUniform1f(mProgressHandler, (System.currentTimeMillis() - mModifyTime) / 500f);
        GLES20.glUniform1i(mDrawFBOHandler, mDrawFBO);

        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(mCoordinateHandler, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glVertexAttrib1f(mAlphaHandler, mAlpha);


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

    private VideoDrawer.TextureCallback callback;


    public void setCallback(VideoDrawer.TextureCallback callback) {
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
        releaseFBO();
        GLES20.glDisableVertexAttribArray(mVertexPosHandler);
        GLES20.glDisableVertexAttribArray(mCoordinateHandler);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteProgram(mProgramId);
    }


    private void releaseFBO() {
        int[] fbs = new int[1];
        fbs[0] = mSoulFrameBuffer;
        int[] texts = new int[1];
        texts[0] = mSoulTextureId;
        OpenGLUtils.deleteFBO(fbs, texts);
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
            sizeRatio[1] = top = verScale / horScale;
            bottom = -top;
        }
        sizeRatio[0] = 1;
        Matrix.orthoM(projectionMatrix, 0, -1, 1, bottom, top, 3, 5);
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
