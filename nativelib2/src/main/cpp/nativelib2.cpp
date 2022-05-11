#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_xzq_nativelib2_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "hello Cmake";
    return env->NewStringUTF(hello.c_str());
}