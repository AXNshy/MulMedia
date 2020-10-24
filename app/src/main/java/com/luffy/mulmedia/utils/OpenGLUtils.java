package com.luffy.mulmedia.utils;

import android.opengl.GLES20;

public class OpenGLUtils {
    public static int[] createTextureId(int count) {
        int[] arrys = new int[count];
        GLES20.glGenTextures(count, arrys, 0);
        return arrys;
    }


    public static int createFBOTexture(int width, int height) {
        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId[0];
    }

    public static int createFrameBuffer() {
        int[] fbs = new int[1];
        GLES20.glGenBuffers(1, fbs, 0);
        return fbs[0];
    }

    public static void bindFBO(int fb, int texture) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);
    }

    public static void unbindFBO() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public static void deleteFBO(int[] fb, int[] texture) {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glDeleteBuffers(1, fb, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, texture, 0);
    }
}
