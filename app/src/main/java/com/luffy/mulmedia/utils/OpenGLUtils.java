package com.luffy.mulmedia.utils;

import android.opengl.GLES20;

public class OpenGLUtils {
    public static int[] createTextureId(int count) {
        int[] arrys = new int[count];
        GLES20.glGenTextures(count, arrys, 0);
        return arrys;
    }
}
