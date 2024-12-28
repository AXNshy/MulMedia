package com.luffyxu.gles2;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.luffyxu.opengles.base.drawer.IDrawer;
import com.luffyxu.opengles.base.utils.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SimpleRenderer implements GLSurfaceView.Renderer {
    private final IDrawer mDrawer;


    public SimpleRenderer(IDrawer mDrawer) {
        this.mDrawer = mDrawer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        mDrawer.setTextureId(OpenGLUtils.createTextureId(1));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mDrawer.setSurfaceSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mDrawer.draw();
    }
}
