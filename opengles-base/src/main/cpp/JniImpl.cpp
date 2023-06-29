//
// Created by Luffy on 2023/4/17.
//


#include <jni.h>
#include <string>
#include <android/native_window.h>
#include "egl/GLESRender.h"
#include <EGL/egl.h>
#include <android/native_window_jni.h>

#ifdef __cplusplus
extern "C" {
#endif

const char *const RENDER_CLASS_NAME = "com/luffyxu/opengles/base/egl/NativeRender";

void native_surface_create(JNIEnv *env, jobject thiz, jint render,jobject surface) {
    GLESRender* nativeRender = reinterpret_cast<GLESRender*>(render);
    ANativeWindow* window = ANativeWindow_fromSurface(env,surface);
    nativeRender->onSurfaceCreate(window);
}
void native_surface_change(JNIEnv *env, jobject thiz, jint render,jobject surface,jint width,jint height) {
    GLESRender* nativeRender = reinterpret_cast<GLESRender*>(render);
    ANativeWindow* window = ANativeWindow_fromSurface(env,surface);
    nativeRender->onSurfaceChanged(window,width,height);
}

void native_surface_destroyed(JNIEnv *env, jobject thiz,jint render) {
    GLESRender* nativeRender = reinterpret_cast<GLESRender*>(render);
    nativeRender->onSurfaceDestroyed();
}


jint native_create_renderer(JNIEnv *env, jobject thiz) {
    GLESRender *nativeRender = new GLESRender(env);
    return reinterpret_cast<jint>(nativeRender);
}

void native_run(JNIEnv *env, jobject thiz,jint render) {
    GLESRender* nativeRender = reinterpret_cast<GLESRender*>(render);
    nativeRender->start();
}



static JNINativeMethod gMethod[] = {
        {"onSurfaceCreated",     "(ILandroid/view/Surface;)V",   (void *) (native_surface_create)},
        {"onSurfaceChanged",     "(ILandroid/view/Surface;II)V", (void *) (native_surface_change)},
        {"onSurfaceDestroyed",   "(I)V",                         (void *) (native_surface_destroyed)},
        {"nativeCreateRenderer", "()I",                          (void *) (native_create_renderer)},
        {"nativeRun",            "(I)V",                         (void *) (native_run)},
};


jint regist_jni_method(JNIEnv *env) {
    jclass clazz = nullptr;
    clazz = env->FindClass(RENDER_CLASS_NAME);
    if (clazz == nullptr) {
        return -1;
    }

    printf("FindClass");

    if (env->RegisterNatives(clazz, gMethod, 5) < 0) {
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