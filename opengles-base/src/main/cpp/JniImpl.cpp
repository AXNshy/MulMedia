//
// Created by Luffy on 2023/4/17.
//
#ifndef EGL_NATIVE_BUFFER_ANDROID 0x3140
#define EGL_NATIVE_BUFFER_ANDROID 0x3140
#endif
#ifndef EGL_IMAGE_PRESERVED_KHR   0x30D2
#define EGL_IMAGE_PRESERVED_KHR   0x30D2
#endif
#ifndef EGL_EGLEXT_PROTOTYPES
#define EGL_EGLEXT_PROTOTYPES
#endif

#include <jni.h>
#include <string>
#include <android/native_window.h>
#include "egl/GLESRender.h"
#include <EGL/egl.h>
#include <android/native_window_jni.h>
#include <android/hardware_buffer_jni.h>

#ifdef __cplusplus
extern "C" {
#endif

const char *const RENDER_CLASS_NAME = "com/luffyxu/opengles/base/egl/NativeRender";

void native_surface_create(JNIEnv *env, jobject thiz, jlong render, jobject surface) {
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(render);
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    nativeRender->onSurfaceCreate(window);
}
void native_surface_change(JNIEnv *env, jobject thiz, jlong render, jobject surface, jint width,
                           jint height) {
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(render);
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    nativeRender->onSurfaceChanged(window, width, height);
}

void native_surface_destroyed(JNIEnv *env, jobject thiz, jlong render) {
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(render);
    nativeRender->onSurfaceDestroyed();
}


jlong native_create_renderer(JNIEnv *env, jobject thiz) {
    GLESRender *nativeRender = new GLESRender(env);
    return reinterpret_cast<jlong>(nativeRender);
}

void native_run(JNIEnv *env, jobject thiz, jlong render) {
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(render);
    nativeRender->start();
}

void nativeUpdateImageBuffer(JNIEnv *env, jobject thiz, jlong native_render, jobject nativeObject) {
    AHardwareBuffer *aHardwareBuffer = AHardwareBuffer_fromHardwareBuffer(env, nativeObject);
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(native_render);
    nativeRender->updateImageBuffer(aHardwareBuffer);
}

char *convertShaderString(JNIEnv *env, jstring str) {
    int length = env->GetStringUTFLength(str);
    const char *cStr = env->GetStringUTFChars(str, JNI_FALSE);
    char *buf = static_cast<char *>(malloc(length + 1));
    memcpy(buf, cStr, length + 1);
    return buf;
}

void nativeInitShader(JNIEnv *env, jobject thiz, jlong native_render, jstring vertexShaderStr,
                      jstring fragShaderStr) {
    GLESRender *nativeRender = reinterpret_cast<GLESRender *>(native_render);
    nativeRender->drawer->setShader(convertShaderString(env, vertexShaderStr),
                                    convertShaderString(env, fragShaderStr));
}

static JNINativeMethod gMethod[] = {
        {"onSurfaceCreated",     "(JLandroid/view/Surface;)V",               (void *) (native_surface_create)},
        {"onSurfaceChanged",     "(JLandroid/view/Surface;II)V",             (void *) (native_surface_change)},
        {"onSurfaceDestroyed",   "(J)V",                                     (void *) (native_surface_destroyed)},
        {"nativeCreateRenderer", "()J",                                      (void *) (native_create_renderer)},
        {"nativeRun",            "(J)V",                                     (void *) (native_run)},
        {"updateImageBuffer",    "(JLandroid/hardware/HardwareBuffer;)V",    (void *) (nativeUpdateImageBuffer)},
        {"nativeInitShader",     "(JLjava/lang/String;Ljava/lang/String;)V", (void *) (nativeInitShader)},
};


jint regist_jni_method(JNIEnv *env) {
    jclass clazz = nullptr;
    clazz = env->FindClass(RENDER_CLASS_NAME);
    if (clazz == nullptr) {
        return -1;
    }

    printf("FindClass");

    if (env->RegisterNatives(clazz, gMethod, 7) < 0) {
        return -1;
    }

    printf("RegisterNatives");
    return JNI_OK;
}

void unregist_jni_method(JNIEnv *env) {
    jclass clazz = nullptr;
    clazz = env->FindClass(RENDER_CLASS_NAME);
    if (clazz == nullptr) {
        return;
    }

    printf("FindClass");

    if (env->UnregisterNatives(clazz) < 0) {
        return;
    }

    printf("UnregisterNatives");
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    jint ret = -1;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK) {
        printf("jvm fail get jniEnv");
        return -1;
    }

    printf("JNI_OnLoad");

//    av_jni_set_java_vm(vm, reserved);

    if (regist_jni_method(env) < 0) {
        printf("fail to register methods");
        return -1;
    }

    return JNI_VERSION_1_4;
}


void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    jint ret = -1;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK) {
        printf("jvm fail get jniEnv");
        return;
    }
    printf("JNI_OnUnload");

    unregist_jni_method(env);
}

#ifdef __cplusplus
}
#endif //__cplusplus
