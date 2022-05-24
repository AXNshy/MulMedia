//
// Created by ZhenqianXu on 2022/5/15.
//

#include <string.h>
#include "native_render.h"

void NativeRender::InitRender(JNIEnv *env, int video_width, int video_height, int *dst_size) {
    m_native_window = ANativeWindow_fromSurface(env,m_surface_ref);
    int windowWidth = ANativeWindow_getWidth(m_native_window);
    int windowHeight = ANativeWindow_getHeight(m_native_window);


    m_dst_w = windowWidth;
    m_dst_h = m_dst_w * video_height / video_width;
    if(m_dst_h > windowHeight){
        m_dst_h = windowHeight;
        m_dst_w = windowHeight * video_width / video_height;
    }

    LOGD(TAG,"window [width:%d,height:%d],  [dst_w:%d,dst_h:%d]",windowWidth,windowHeight,m_dst_w,m_dst_h);

    //设置window缓冲区的格式和大小
    ANativeWindow_setBuffersGeometry(m_native_window,windowWidth,windowHeight,WINDOW_FORMAT_RGBA_8888);

    dst_size[0] = m_dst_w;
    dst_size[1] = m_dst_h;
}

void NativeRender::Render(OneFrame *frame) {
    ANativeWindow_lock(m_native_window,&m_out_buffer, nullptr);

    u_int8_t *dst = static_cast<u_int8_t *>(m_out_buffer.bits);
    int dstStride = m_out_buffer.stride * 4;
    int srcStride = frame->line_size;

    for(int32_t h = 0;h < m_dst_h;h++) {
        //change data copy order in Y axis
        memcpy(dst + dstStride * (m_dst_h - h - 1), frame->data + h * srcStride, srcStride);
    }

    ANativeWindow_unlockAndPost(m_native_window);

}

void NativeRender::ReleaseRender() {
    if(m_native_window != nullptr){
        ANativeWindow_release(m_native_window);
    }


}

NativeRender::NativeRender(JNIEnv *env, jobject surface) {
    m_surface_ref = env->NewGlobalRef(surface);
}

NativeRender::~NativeRender() {
}
