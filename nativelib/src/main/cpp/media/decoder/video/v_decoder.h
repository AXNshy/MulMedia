//
// Created by ZhenqianXu on 2022/5/13.
//

#ifndef MULMEDIA_V_DECODER_H
#define MULMEDIA_V_DECODER_H


#include "../base_decoder.h"
#include "../../render/video/video_render.h"

class VideoDecoder : public BaseDecoder{
private:
    const char *TAG = "VideoDecoder";

    const AVPixelFormat DST_FORMAT = AV_PIX_FMT_ABGR;

    AVFrame *m_rgb_frame = nullptr;

    uint8_t *m_buf_rgb_frame = nullptr;

    SwsContext *m_sws_ctx = nullptr;

    VideoRender *m_video_render = nullptr;
    //目标宽高
    int m_dst_w;
    int m_dst_h;

    void InitRender(JNIEnv *env);

    void InitBuffer();

    void InitSws();


public:
    VideoDecoder(JNIEnv *env, _jstring *const path, bool for_synthesizer = false);
    virtual ~VideoDecoder();

    void SetRender(VideoRender *render);

protected:

    AVMediaType GetMediaType() override{
        return AVMEDIA_TYPE_VIDEO;
    }


    bool NeedDecodeLoop() override;

    void Prepare(JNIEnv *env) override;

    void Render(AVFrame *frame) override;

    void Release() override;

    const char *const LogSpec() override {
        return "VIDEO";
    }

    int width();

    int height();

    enum AVPixelFormat video_pixel_format();
};


#endif //MULMEDIA_V_DECODER_H
