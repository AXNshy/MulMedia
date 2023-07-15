//
// Created by Luffy on 2023/4/17.
//

#ifndef MULMEDIA_GLESRENDER_H
#define MULMEDIA_GLESRENDER_H

#define GL_GLEXT_PROTOTYPES
#define EGL_NATIVE_BUFFER_ANDROID 0x3140
#define EGL_IMAGE_PRESERVED_KHR   0x30D2
#define EGL_EGLEXT_PROTOTYPES

#include "../render/IDrawer.h"
#include "RenderState.h"

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <GLES2/gl2ext.h>


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

    IDrawer *drawer = NULL;

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

    bool createEglSurface();

    void destroyEGLSurface();

    void releaseEGL();

    void draw();

    void configWorldSize();

    void updateImageBuffer(AHardwareBuffer *buffer);

    void setDrawer(IDrawer *drawer1) {
        drawer = drawer1;
    }

    void createEglKHRTexture(AHardwareBuffer *buffer);

protected:
    int width = -1;
    int height = -1;
    RenderEGLContext *eglContext = NULL;

private:
    const char *TAG = "GLES_RENDERER";

    JavaVM *m_jvm_for_thread = NULL;

    ANativeWindow *nativeWindow = NULL;

    pthread_mutex_t m_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t m_cont = PTHREAD_COND_INITIALIZER;

    bool is_egl_create = false;

    int current_state = 0;


    AHardwareBuffer *buffer = NULL;

    void checkEglError(char *msg);
};

#endif //MULMEDIA_GLESRENDER_H
