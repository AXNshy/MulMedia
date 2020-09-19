package com.luffy.mulmedia.opengl;

public interface IDrawer {
    void draw();

    void setTextureId(int id);

    void release();

    void setSurfaceSize(int w, int h);

    void setVideoSize(int w, int h);

    void translate(int translateX, int translateY);

    void scale(int scaleX, int scaleY);
}
