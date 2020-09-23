package com.luffy.mulmedia.opengl;

public interface IDrawer {
    void draw();

    void setTextureId(int id);

    void release();

    void setSurfaceSize(int w, int h);

    void setVideoSize(int w, int h);

    void translate(float translateX, float translateY);

    void scale(float scaleX, float scaleY);
}
