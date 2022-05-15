//
// Created by ZhenqianXu on 2022/4/23.
//


#ifndef MULMEDIA_MEDIA_H
#define MULMEDIA_MEDIA_H

#include <jni.h>
#include "decoder/video/v_decoder.h"
#include "render/video/video_render.h"
class FFmpegPlayer{
private:
    VideoDecoder *m_decoder;

    VideoRender *m_render;

public:
    FFmpegPlayer(JNIEnv *env,jstring path,jobject surface);
    ~FFmpegPlayer();

    void play();

    void pause();

    void stop();
};

#endif //MULMEDIA_MEDIA_H
