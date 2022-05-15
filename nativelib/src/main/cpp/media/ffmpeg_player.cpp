//
// Created by ZhenqianXu on 2022/4/23.
//

#include "ffmpeg_player.h"
#include "render/video/native_render/native_render.h"

FFmpegPlayer::FFmpegPlayer(JNIEnv *env, jstring path, jobject surface) {
    m_decoder = new VideoDecoder(env,path);
    m_render = new NativeRender(env,surface);
    m_decoder->SetRender(m_render);
}


FFmpegPlayer::~FFmpegPlayer() {

}

void FFmpegPlayer::play() {
    if(m_decoder != nullptr){
        m_decoder->GoOn();
    }
}

void FFmpegPlayer::pause() {
    if(m_decoder != nullptr){
        m_decoder->Pause();
    }
}

void FFmpegPlayer::stop() {
    if(m_decoder != nullptr){
        m_decoder->Stop();
    }
}

