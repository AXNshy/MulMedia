//
// Created by ZhenqianXu on 2022/4/23.
//

#include "ffmpeg_player.h"
#include "render/video/native_render/native_render.h"
#include "render/audio/opensl/opensl_render.h"

FFmpegPlayer::FFmpegPlayer(JNIEnv *env, jstring path, jobject surface) {
    m_v_decoder = new VideoDecoder(env, path, true);
    m_v_render = new NativeRender(env, surface);
    m_v_decoder->SetRender(m_v_render);

    m_a_decoder = new AudioDecoder(env, path, false);
    m_a_render = new OpenSLRender();
    m_a_decoder->SetRender(m_a_render);

}


FFmpegPlayer::~FFmpegPlayer() {

}

void FFmpegPlayer::play() {
    if(m_v_decoder != nullptr){
        m_v_decoder->GoOn();
    }

    if(m_a_decoder != nullptr){
        m_a_decoder->GoOn();
    }
}

void FFmpegPlayer::pause() {
    if(m_v_decoder != nullptr){
        m_v_decoder->Pause();
    }

    if(m_a_decoder != nullptr){
        m_a_decoder->Pause();
    }
}

void FFmpegPlayer::stop() {
    if(m_v_decoder != nullptr){
        m_v_decoder->Stop();
    }

    if(m_a_decoder != nullptr){
        m_a_decoder->Stop();
    }
}

