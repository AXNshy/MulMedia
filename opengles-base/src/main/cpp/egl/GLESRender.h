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
#include <GLES3/gl3.h>

using namespace std;

#include <thread>
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
    EGLContext context;
} ;


class GLESRender {
public:

    GLESRender(JNIEnv *env);

    virtual ~GLESRender();

    void holdOn();

    void notifyGo();

    void onSurfaceCreate(ANativeWindow *surface);

    void onSurfaceChanged(ANativeWindow *surface, int width, int height);

    void onSurfaceDestroyed();

    void start();

    static void run(std::shared_ptr<GLESRender>);

    void loopRender();

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
    int width = -1;
    int height = -1;
    RenderEGLContext eglContext;


    virtual void thread_launch();

private:
    const char *TAG = "GLES_RENDERER";

    JavaVM *m_jvm_for_thread;

    ANativeWindow *nativeWindow;

    pthread_mutex_t m_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t m_cont = PTHREAD_COND_INITIALIZER;

    bool is_egl_create = false;

    int current_state = 0;

    IDrawer *drawer;
};

#endif //MULMEDIA_GLESRENDER_H
