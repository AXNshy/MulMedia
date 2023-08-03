//
// Created by ZhenqianXu on 2022/5/13.
//

#include "a_decoder.h"

void AudioDecoder::InitSwr() {
    AVCodecContext *codec_ct = codec_ctx();

    m_swr = swr_alloc();
    struct AVChannelLayout oh = AV_CHANNEL_LAYOUT_STEREO;

    swr_alloc_set_opts2(&m_swr, &oh, GetSampleFmt(), GetSampleRate(codec_ct->sample_rate),
                        &codec_ct->ch_layout, codec_ct->sample_fmt, codec_ct->sample_rate, 0,
                        nullptr);
//    av_opt_set_int(m_swr,"in_channel_layout",codec_ct->channel_layout,0);
//    av_opt_set_int(m_swr,"out_channel_layout",ENCODE_AUDIO_DEST_CHANNEL_LAYOUT     ,0);
//    av_opt_set_int(m_swr,"in_sample_rate",codec_ct->sample_rate,0);
//    av_opt_set_int(m_swr,"out_sample_rate",GetSampleRate(codec_ct->sample_rate),0);

    av_opt_set_sample_fmt(m_swr, "in_sample_fmt", codec_ct->sample_fmt, 0);
    av_opt_set_sample_fmt(m_swr, "out_sample_fmt", GetSampleFmt(), 0);
    swr_init(m_swr);

    LOGI(TAG, "sample rate: %d, channel: %d, format: %d, frame_size: %d, layout: %lld",
         codec_ct->sample_rate, codec_ct->channels, codec_ct->sample_fmt, codec_ct->frame_size,
         codec_ct->channel_layout)

}

void AudioDecoder::CalculateSampleArgs() {
    m_dst_nb_sample = av_rescale_rnd(ACC_NB_SAMPLES, GetSampleRate(codec_ctx()->sample_rate),
                                     codec_ctx()->sample_rate, AV_ROUND_UP);

    m_dst_sample_size = av_samples_get_buffer_size(nullptr, ENCODE_AUDIO_DEST_CHANNEL_COUNTS,
                                                   m_dst_nb_sample, GetSampleFmt(), 1);
}

void AudioDecoder::InitOutputBuffer() {
    if (ForSynthesizer()) {
        m_out_buffer[0] = static_cast<uint8_t *>(malloc(m_dst_sample_size / 2));
        m_out_buffer[1] = static_cast<uint8_t *>(malloc(m_dst_sample_size / 2));
    } else {
        m_out_buffer[0] = static_cast<uint8_t *>(malloc(m_dst_sample_size));
    }

}

void AudioDecoder::InitRender() {
    if (m_render != nullptr) {
        m_render->InitRender(nullptr);
    }
}

void AudioDecoder::ReleaseOutputBuffer() {
    if (m_out_buffer[0] != nullptr) {
        free(m_out_buffer[0]);
        m_out_buffer[0] = nullptr;
    }
}

AudioDecoder::AudioDecoder(JNIEnv *env, const jstring path, bool forSynthesizer) : BaseDecoder(env,
                                                                                               path,
                                                                                               forSynthesizer) {
}

AudioDecoder::~AudioDecoder() {
    delete m_render;
}

void AudioDecoder::SetRender(AudioRender *render) {
    m_render = render;
}

void AudioDecoder::Prepare(JNIEnv *env) {
    InitRender();
    CalculateSampleArgs();
    InitOutputBuffer();
    InitSwr();
}

void AudioDecoder::Render(AVFrame *frame) {
    InitOutputBuffer();

    int ret = swr_convert(m_swr, m_out_buffer, m_dst_sample_size / ENCODE_AUDIO_DEST_CHANNEL_COUNTS,
                          (const uint8_t **) (frame->data), frame->nb_samples);

    if (ret > 0) {
        if (ForSynthesizer()) {

        } else {
            LOGD(TAG, "data : size[%d]", m_dst_sample_size);


            if (m_render != nullptr) {
                m_render->Render(m_out_buffer[0], (size_t) m_dst_sample_size);
            }
        }
    }
}

void AudioDecoder::Release() {
    if (m_swr != NULL) {
        swr_free(&m_swr);
    }
    if (m_render != NULL) {
        m_render->ReleaseRender();
    }
    ReleaseOutputBuffer();
}
