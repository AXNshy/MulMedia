//
// Created by Luffy on 2023/4/17.
//

#ifndef MULMEDIA_GLESRENDER_H
#define MULMEDIA_GLESRENDER_H



#include "../render/IDrawer.h"
#include "RenderState.h"


#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <EGL/eglplatform.h>
#include <EGL/egl.h>


//
//const int NO_SURFACE = 0x00;
//const int FRESH_SURFACE = 0x01;
//const int SURFACE_CHANGE = 0x02;
//const int RENDERING = 0x03;
//const int SURFACE_DESTROY = 0x04;
//const int STOP = 0x05;


struct RenderEGLContext{
    EGLint* versions;
    EGLDisplay display;
    EGLConfig config;
    EGLSurface surface;
} ;


class GLESRender {
public:
    int current_state = 0;
    bool is_egl_created = false;
    bool is_egl_binded = false;
    int *lock = nullptr;

    IDrawer* drawer;

    GLESRender();

    virtual ~GLESRender();

    void holdOn();

    void notifyGo();

    void onSurfaceCreate(ANativeWindow* surface);

    void onSurfaceChanged(ANativeWindow* surface,int width, int height);

    void onSurfaceDestroyed();

    void run();

    bool init();

    void release();

    void createEglSurfaceFirst();

    void createEglSurface();

    void destroyEGLSurface();

    void releaseEGL();

    void draw();

    void configWorldSize();

    void setDrawer(IDrawer drawer1){
        drawer = &drawer1;
    }

protected:

    const char* TAG = "GLES_RENDERER";
    int width = -1;
    int height= -1;
    RenderEGLContext eglContext;
    virtual void thread_launch();
};

#endif //MULMEDIA_GLESRENDER_H
