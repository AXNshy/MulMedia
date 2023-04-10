package com.luffy.mulmedia.gles2;

public interface IDrawer {
    void draw();

    void setTextureId(int[] id);

    void release();

    void setSurfaceSize(int w, int h);

    void setVideoSize(int w, int h);

    void translate(float translateX, float translateY);

    void scale(float scaleX, float scaleY);

    void setShader(IGLShader shader);
}
