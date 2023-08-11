#include <jni.h>
#include <string>
//#include <libavcodec/jni.h>
#include "media/ffmpeg_player.h"

#ifdef __cplusplus
extern "C" {
#endif
//JNIEXPORT jstring JNICALL
//Java_com_xzq_nativelib_FFmpegPlayer_ffmpegInfo(JNIEnv *env, jobject thiz) {
//}

jstring native_ffmpegInfo(JNIEnv *env, jobject thiz) {
    return (jstring) "";
}

jint native_createPlayer(JNIEnv *env, jobject thiz, jstring path,
                  jobject surface) {
    FFmpegPlayer *player = new FFmpegPlayer(env, path, surface);
    return reinterpret_cast<jlong>(player);
}

//JNIEXPORT jint JNICALL
//Java_com_xzq_nativelib_FFmpegPlayer_createPlayer(JNIEnv *env, jobject thiz, jstring path,
//                                                 jobject surface) {
//    FFmpegPlayer *player = new FFmpegPlayer(env, path, surface);
//    return reinterpret_cast<jint>(player);
//}

void native_play(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->play();
}

//JNIEXPORT void JNICALL
//Java_com_xzq_nativelib_FFmpegPlayer_play(JNIEnv *env, jobject thiz, jint playerRef) {
//    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
//    player->play();
//}

//JNIEXPORT void JNICALL
//Java_com_xzq_nativelib_FFmpegPlayer_pause(JNIEnv *env, jobject thiz, jint playerRef) {
//    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
//    player->pause();
//}

void native_pause(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->pause();
}

//JNIEXPORT void JNICALL
//Java_com_xzq_nativelib_FFmpegPlayer_stop(JNIEnv *env, jobject thiz, jint playerRef) {
//    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
//    player->stop();
//}


void native_stop(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->stop();
}


//JNIEXPORT jstring JNICALL
//Java_com_xzq_nativelib_NativeLib_stringFromJNI(JNIEnv *env, jobject /* this */) {
//    std::string hello = "Hello from C++ to Java";
//    return env->NewStringUTF(hello.c_str());
//}

//JNIEXPORT void JNICALL Java_com_xzq_nativelib_NativeLib_hello(JNIEnv *env, jobject /* this */) {
////        return env -> NewStringUTF(hello. c_str()) ;
//    char *hello = "Hello from C++";
//    printf("in native:%s", hello);
//}


static JNINativeMethod gMethod[] = {
        {"ffmpegInfo", "()Ljava/lang/String;",                        (void *) (native_ffmpegInfo)},
        {"createPlayer", "(Ljava/lang/String;Landroid/view/Surface;)I", (void *) (native_createPlayer)},
        {"play",         "(I)V",                                       (void *) (native_play)},
        {"pause",        "(I)V",                                       (void *) (native_pause)},
        {"stop",         "(I)V",                                       (void *) (native_stop)},
};


jint regist_jni_method(JNIEnv *env) {
    jclass clazz = nullptr;
    clazz = env->FindClass("com/luffyxu/ffmpeg/FFmpegPlayer");
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
    clazz = env->FindClass("com/luffyxu/ffmpeg/FFmpegPlayer");
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