//
// Created by ZhenqianXu on 2022/5/13.
//

#ifndef MULMEDIA_A_DECODER_H
#define MULMEDIA_A_DECODER_H

#include "../base_decoder.h"

#include "../../render/audio/audio_render.h"
extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <libavutil/audio_fifo.h>
};

class AudioDecoder : public BaseDecoder {
private:
    const char *TAG = "AudioDecoder";

    SwrContext *m_swr = nullptr;

    AudioRender *m_render = nullptr;

    uint8_t *m_out_buffer[2] = {nullptr, nullptr};

    int m_dst_nb_sample = 1024;

    size_t m_dst_sample_size = 0;

    AVSampleFormat ENCODE_AUDIO_DST_FORMAT = AV_SAMPLE_FMT_FLTP;
    static const uint64_t ENCODE_AUDIO_DEST_CHANNEL_LAYOUT = AV_CH_LAYOUT_STEREO;
    static const int32_t ENCODE_AUDIO_DST_SAMPLE_RATE = 44100;
// 音频编码通道数
    static const int ENCODE_AUDIO_DEST_CHANNEL_COUNTS = 2;

    // 音频编码比特率
    static const int ENCODE_AUDIO_DEST_BIT_RATE = 64000;

    // ACC音频一帧采样数
    static const int ACC_NB_SAMPLES = 1024;

    void InitSwr();

    void CalculateSampleArgs();

    void InitOutputBuffer();

    void InitRender();

    void ReleaseOutputBuffer();

    AVSampleFormat GetSampleFmt() {
        if (ForSynthesizer()) {
            return ENCODE_AUDIO_DST_FORMAT;
        } else {
            return AV_SAMPLE_FMT_S16;
        }
    }

public:
    AudioDecoder(JNIEnv* env,const jstring path, bool forSynthesizer);
    ~AudioDecoder();

    void SetRender(AudioRender *render);


protected:

    void Prepare(JNIEnv *env) override;

    void Render(AVFrame *frame) override;

    void Release() override;

    AVMediaType GetMediaType() override {
        return AVMEDIA_TYPE_AUDIO;
    }

    bool NeedDecodeLoop() override {
        return true;
    }

    const char *const LogSpec() override {
        return "AUDIO";
    }

    int GetSampleRate(int rate){
        if(ForSynthesizer()){
            return ENCODE_AUDIO_DST_SAMPLE_RATE;
        } else{
            return rate;
        }
    }
};


#endif //MULMEDIA_A_DECODER_H
