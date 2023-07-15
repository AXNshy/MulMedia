//
// Created by Luffy on 2023/4/17.
//

#include "GLESRender.h"
#include "../utils/logger.h"
#include "../render/drawer/TextureDrawer.h"


GLESRender::GLESRender(JNIEnv *env) {
    LOGD(TAG, "GLESRender constructor")
    env->GetJavaVM(&m_jvm_for_thread);

    IDrawer *d = new TextureDrawer();
    setDrawer(d);
}

GLESRender::~GLESRender() {

}

void GLESRender::holdOn() {
    pthread_mutex_lock(&m_mutex);
    pthread_cond_wait(&m_cont, &m_mutex);
    pthread_mutex_unlock(&m_mutex);
}

void GLESRender::notifyGo() {
    pthread_mutex_lock(&m_mutex);
    pthread_cond_signal(&m_cont);
    pthread_mutex_unlock(&m_mutex);
}

void GLESRender::onSurfaceCreate(ANativeWindow* surface) {
    LOGD(TAG, "onSurfaceCreate")
    nativeWindow = surface;
    current_state = FRESH_SURFACE;
    notifyGo();
}

void GLESRender::onSurfaceChanged(ANativeWindow* surface,int width, int height) {
    LOGD(TAG, "onSurfaceChanged width:%d,height:%d", width, height)
    this->width = width;
    this->height = height;
    current_state = SURFACE_CHANGE;
    notifyGo();
}

void GLESRender::onSurfaceDestroyed() {
    LOGD(TAG, "onSurfaceDestroyed")
    nativeWindow = nullptr;
    current_state = SURFACE_DESTROY;
    notifyGo();
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
    LOGD(TAG, "init")
    eglContext = new RenderEGLContext();
    EGLDisplay eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        LOGE(TAG, "eglGetDisplay fail")
        return false;
    }
    eglContext->display = eglDisplay;
    EGLint *ver = new EGLint[2];
    if (!eglInitialize(eglDisplay, &ver[0], &ver[1])) {
        LOGE(TAG, "eglInitialize fail")
        return false;
    }
    LOGE(TAG, "eglVersion %d.%d", ver[0], ver[1]);
    eglContext->versions = ver;
    EGLConfig *config = new EGLConfig[1];
    EGLint numConfig[] = {EGL_NONE};

    if (eglChooseConfig(eglDisplay, configAttribe, config, 1, numConfig) == EGL_FALSE) {
        LOGE(TAG, "eglChooseConfig fail")
        return false;
    }

    if (config[0] == nullptr) {
        LOGE(TAG, "eglConfig is null")
        return false;
    }
    eglContext->config = config[0];
    return true;
}

void GLESRender::release() {
    current_state = NO_SURFACE;
    notifyGo();
}

void GLESRender::run(std::shared_ptr<GLESRender> that) {
    JNIEnv *env = nullptr;
    if (that->m_jvm_for_thread->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        LOGE(that->TAG, "fail to init render thread");
        return;
    }

    if (!that->init()) {
        LOGE(that->TAG, "RENDERER init fail")
        return;
    }
    try {
        that->loopRender();
    } catch (int e) {

    }
}

void GLESRender::createEglSurfaceFirst() {
    glViewport(0, 0, width, height);
    if (!is_egl_create) {
        is_egl_create = true;
        createEglSurface();
//        drawer->init();
        drawer->init();
    }
}

bool GLESRender::createEglSurface() {
    LOGE(TAG, "createEglSurface")
    if (nativeWindow == nullptr) {
        LOGE(TAG, "nativeWindow is null")
        return false;
    }
    EGLSurface surface;
    int attris[] = {EGL_NONE};
    surface = eglCreateWindowSurface(eglContext->display, eglContext->config, nativeWindow,
                                     attris);
    if (surface == EGL_NO_SURFACE) {
        LOGE(TAG, "eglCreateWindowSurface fail")
        return false;
    }
    eglContext->surface = surface;

    int contextAttris[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    EGLContext context;
    context = eglCreateContext(eglContext->display, eglContext->config, nullptr, contextAttris);
    if (context == EGL_NO_CONTEXT) {
        LOGE(TAG, "eglCreateContext fail")
        return false;
    }
    eglContext->context = context;
    if (!eglMakeCurrent(eglContext->display, eglContext->surface, eglContext->surface,
                        eglContext->context)) {
        LOGE(TAG, "eglMakeCurrent fail")
        return false;
    }
    return true;
}

void GLESRender::destroyEGLSurface() {
    eglMakeCurrent(eglContext->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    is_egl_create = false;
}

void GLESRender::releaseEGL() {
    LOGE(TAG, "releaseEGL")
    eglMakeCurrent(eglContext->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(eglContext->display, eglContext->context);
    eglReleaseThread();
    eglTerminate(eglContext->display);
    eglContext->display = EGL_NO_DISPLAY;
    eglContext->context = EGL_NO_CONTEXT;
    eglContext->surface = EGL_NO_SURFACE;
    is_egl_create = false;
}

void GLESRender::draw() {
    LOGD(TAG, "draw %p", eglContext->context)
    EGLint eglImgAttrs[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE, EGL_NONE};
    EGLClientBuffer eglClientBuffer = eglGetNativeClientBufferANDROID(buffer);
    checkEglError("eglGetNativeClientBufferANDROID");
    EGLImageKHR eglImageKhr = eglCreateImageKHR(eglContext->display, eglContext->context,
                                                EGL_NATIVE_BUFFER_ANDROID,
                                                eglClientBuffer,
                                                eglImgAttrs);
    checkEglError("eglCreateImageKHR");
    drawer->setEGLImage(eglImageKhr);

    drawer->draw();
    eglSwapBuffers(eglContext->display, eglContext->surface);
}

void GLESRender::configWorldSize() {
    drawer->setViewSize(width, height);
}

void GLESRender::start() {
    std::shared_ptr<GLESRender> that(this);
    std::thread td(run, that);
    td.detach();
}

void GLESRender::loopRender() {
    LOGD(TAG, "loopRender start")
    while (true) {
        LOGD(TAG, "loopRender current_state:%d", current_state)
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
                current_state = NO_SURFACE;
                break;
            case STOP:
                releaseEGL();
                drawer->release();
                goto out;
            default:
                holdOn();
                break;
        }
        this_thread::sleep_for(std::chrono::microseconds(20));
    }
    out:
    LOGD(TAG, "GLESRender release")
}

void GLESRender::updateImageBuffer(AHardwareBuffer *buffer) {
    LOGD(TAG, "updateImageBuffer")
    createEglKHRTexture(buffer);
}

void GLESRender::createEglKHRTexture(AHardwareBuffer *buffer) {
    LOGD(TAG, "createEglKHRTexture")
    this->buffer = buffer;
}

void GLESRender::checkEglError(char *msg) {
    int error = eglGetError();
    if (error != EGL_SUCCESS) {
        LOGE(TAG, "%s getError is %d", msg, error);
    }
}
