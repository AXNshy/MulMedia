//
// Created by ZhenqianXu on 2022/4/23.
//


#ifndef MULMEDIA_MEDIA_H
#define MULMEDIA_MEDIA_H

#include <jni.h>
#include "decoder/video/v_decoder.h"
#include "render/video/video_render.h"
#include "render/audio/audio_render.h"
#include "decoder/audio/a_decoder.h"

#include <android/native_window.h>


class FFmpegPlayer{
private:
    VideoDecoder *m_v_decoder;
    AudioDecoder *m_a_decoder;

    VideoRender *m_v_render;
    AudioRender *m_a_render;

public:
    FFmpegPlayer(JNIEnv *env,jstring path,jobject surface);
    ~FFmpegPlayer();

    void play();

    void pause();

    void stop();
};

#endif //MULMEDIA_MEDIA_H
