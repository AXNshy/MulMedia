#include <jni.h>
#include <string>
#include "media/ffmpeg_player.h"

extern "C" {
    JNIEXPORT jstring JNICALL Java_com_xzq_nativelib_NativeLib_stringFromJNI(JNIEnv *env,jobject /* this */) {
        std::string hello = "Hello from C++ to Java";
        return env->NewStringUTF(hello.c_str());
    }

    JNIEXPORT void JNICALL Java_com_xzq_nativelib_NativeLib_hello(JNIEnv * env ,jobject /* this */) {
//        return env -> NewStringUTF(hello. c_str()) ;
        char *hello = "Hello from C++";
        printf("in native:%s", hello);
    }
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_xzq_nativelib_FFmpegPlayer_ffmpegInfo(JNIEnv *env, jobject thiz) {
    // TODO: implement ffmpegInfo()
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_xzq_nativelib_FFmpegPlayer_createPlayer(JNIEnv *env, jobject thiz, jstring path,
                                                 jobject surface) {
    FFmpegPlayer *player = new FFmpegPlayer(env,path,surface);
    return reinterpret_cast<jint>(player);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_xzq_nativelib_FFmpegPlayer_play(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->play();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_xzq_nativelib_FFmpegPlayer_pause(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->pause();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_xzq_nativelib_FFmpegPlayer_stop(JNIEnv *env, jobject thiz, jint playerRef) {
    FFmpegPlayer *player = reinterpret_cast<FFmpegPlayer *>(playerRef);
    player->pause();
}