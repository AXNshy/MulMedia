#include <jni.h>
#include <string>

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