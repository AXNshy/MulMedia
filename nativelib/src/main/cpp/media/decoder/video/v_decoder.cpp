//
// Created by ZhenqianXu on 2022/5/13.
//

#include "v_decoder.h"

VideoDecoder::VideoDecoder(JNIEnv *env, _jstring *const path, bool for_synthesizer) : BaseDecoder(env, path,for_synthesizer) {

}



VideoDecoder::~VideoDecoder() {

}

/*
 * 视频解码器初始化
 * */
void VideoDecoder::Prepare(JNIEnv *env){
    InitRender(env);
    InitBuffer();
    InitSws();
}

/*
 * 视频解码器初始化
 * */
void VideoDecoder::InitRender(JNIEnv *env){
    if(m_video_render != nullptr){
        int dst_size[2] = {-1,-1};
        m_video_render->InitRender(env,width(),height(),dst_size);
        m_dst_w = dst_size[0];
        m_dst_h = dst_size[1];

        if(m_dst_w == -1){
            m_dst_w = width();
        }

        if(m_dst_h == -1){
            m_dst_h = height();
        }

        LOGD(TAG,"dst [%d,%d]",m_dst_w,m_dst_h);
    } else{
        LOGE(TAG,"need call SetRender first");
    }

}

/*
 * 初始化
 * 1.初始化AVFrame结构体并设置默认值，还没有分配buffer内存
 * 2.获取存储指定像素格式的一幅图像所需数据量的大小，以字节为单位
 * 3.申请一幅图像所需内存
 * 4.将3中申请的内存分配给AVFrame缓存
 * */
void VideoDecoder::InitBuffer() {
    m_rgb_frame = av_frame_alloc();
    int numBytes = av_image_get_buffer_size(DST_FORMAT,m_dst_w,m_dst_h,1);

    m_buf_rgb_frame = static_cast<uint8_t *>(av_malloc(numBytes * sizeof(u_int8_t)));
    av_image_fill_arrays(m_rgb_frame->data,m_rgb_frame->linesize,m_buf_rgb_frame,DST_FORMAT,m_dst_w,m_dst_h,1);
}

/*
 * 初始化缩放工具上下文
 * */
void VideoDecoder::InitSws() {
    m_sws_ctx = sws_getContext(width(),height(),video_pixel_format(),m_dst_w,m_dst_h,DST_FORMAT,SWS_FAST_BILINEAR,
                               nullptr, nullptr, nullptr);

}

int VideoDecoder::width() {
    return parameters->width;
}

int VideoDecoder::height() {
    return parameters->height;
}

enum AVPixelFormat VideoDecoder::video_pixel_format() {
    return AV_PIX_FMT_X2BGR10LE;
}



void VideoDecoder::Release() {
    LOGD(TAG,LogSpec(),"[VIDEO] release")
    if(m_rgb_frame != nullptr){
        av_frame_free(&m_rgb_frame);
        m_rgb_frame = nullptr;
    }

    if(m_buf_rgb_frame != nullptr){
        free(m_buf_rgb_frame);
        m_buf_rgb_frame = nullptr;
    }

    if(m_sws_ctx != nullptr){
        sws_freeContext(m_sws_ctx);
        m_sws_ctx = nullptr;
    }

    if(m_video_render != nullptr){
        m_video_render->ReleaseRender();
        m_video_render = nullptr;
    }
}

void VideoDecoder::Render(AVFrame *frame) {
    sws_scale(m_sws_ctx,frame->data,frame->linesize,0,height(),m_rgb_frame->data,m_rgb_frame->linesize);

    OneFrame* oneFrame = new OneFrame(m_rgb_frame->data[0],m_rgb_frame->linesize[0],m_rgb_frame->pts,m_rgb_frame->time_base,
                                   nullptr, false);
    m_video_render->Render(oneFrame);

}

bool VideoDecoder::NeedDecodeLoop() {
    return false;
}

void VideoDecoder::SetRender(VideoRender *render) {
    m_video_render = render;
}