//
// Created by Luffy on 2023/4/17.
//

#include "GLESRender.h"
#include "../utils/logger.h"


GLESRender::GLESRender() {
}

GLESRender::~GLESRender() {

}

void GLESRender::holdOn() {

}

void GLESRender::notifyGo() {

}

void GLESRender::onSurfaceCreate(ANativeWindow* surface) {

}

void GLESRender::onSurfaceChanged(ANativeWindow* surface,int width, int height) {
    this->width = width;
    this->height = height;
}

void GLESRender::onSurfaceDestroyed() {

}

EGLint configAttribe[] = {
        EGL_SURFACE_TYPE,
        EGL_WINDOW_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_DEPTH_SIZE, 24,
        EGL_NONE
};

bool GLESRender::init() {
    EGLDisplay eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if(eglDisplay == EGL_NO_DISPLAY){
        LOGE(TAG, "eglGetDisplay fail")
        return false;
    }
    eglContext.display = eglDisplay;
    EGLint* ver = new EGLint[2];
    if( eglInitialize(eglDisplay,&ver[0],&ver[1])){
        LOGE(TAG, "eglInitialize fail")
        return false;
    }
    eglContext.versions = ver;
    EGLConfig* config;
    EGLint * numConfig = {reinterpret_cast<EGLint *>(EGL_NONE)};

    if(eglChooseConfig(eglDisplay,configAttribe,config,(EGLint)1,numConfig)){
        LOGE(TAG, "eglChooseConfig fail")
        return false;
    }

    if(config[0] == nullptr){
        LOGE(TAG, "eglConfig is null")
        return false;
    }
    eglContext.config = config[0];
    return true;
}

void GLESRender::release() {

}

void GLESRender::run() {
    if(init()){
        LOGE(TAG,"RENDERER init fail")
        return;
    }
    try {
        while (true){
            switch (current_state) {
                case RENDERING:
                    draw();
                    break;
                case FRESH_SURFACE:
                    createEglSurfaceFirst();
                    holdOn();
                    break;
                case SURFACE_CHANGE:
                    createEglSurfaceFirst();
                    configWorldSize();
                    current_state = RENDERING;
                    break;
                case SURFACE_DESTROY:
                    destroyEGLSurface();
                    break;
                case STOP:
                    releaseEGL();
                    goto out;
            }
        }
    } catch (int e) {

    }

    out:
    drawer->release();
}

void GLESRender::createEglSurfaceFirst() {

}

void GLESRender::createEglSurface() {

}

void GLESRender::destroyEGLSurface() {

}

void GLESRender::releaseEGL() {

}

void GLESRender::draw() {

}

void GLESRender::configWorldSize() {

}

void GLESRender::thread_launch() {

}



