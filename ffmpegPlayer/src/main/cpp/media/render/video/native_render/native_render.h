//
// Created by ZhenqianXu on 2022/5/15.
//

#ifndef MULMEDIA_NATIVE_RENDER_H
#define MULMEDIA_NATIVE_RENDER_H


#include "../video_render.h"

extern "C" {
    #include <android/native_window.h>
    #include <android/native_window_jni.h>
};
class NativeRender : public VideoRender{

private:
    const char *TAG = "NativeRender";

    jobject m_surface_ref = nullptr;

    /*
     *渲染数据
     * */
    ANativeWindow_Buffer m_out_buffer;

    /*
     * Android 本地窗口，对应了Java层 Surface对象
     * 图像数据队列的生产者端
     * */
    ANativeWindow *m_native_window = nullptr;


    int m_dst_w;
    int m_dst_h;

public:
    NativeRender(JNIEnv *env,jobject surface);

    ~NativeRender();

    void InitRender(JNIEnv *env, int video_width, int video_height, int *dst_size) override;

    void Render(OneFrame *frame) override;

    void ReleaseRender() override;
};


#endif //MULMEDIA_NATIVE_RENDER_H
