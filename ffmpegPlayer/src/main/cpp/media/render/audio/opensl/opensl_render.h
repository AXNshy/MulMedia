//
// Created by ZhenqianXu on 2022/5/21.
//

#ifndef MULMEDIA_OPENSL_RENDER_H
#define MULMEDIA_OPENSL_RENDER_H


#include <queue>
#include <string>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "../audio_render.h"
#include <pthread.h>
#include <libavformat/avformat.h>

using namespace std;

class OpenSLRender : public AudioRender {
private:
    class PCMData {
    public:
        PCMData(uint8_t *data, int size){
            this->data = data;
            this->size = size;
        }

        uint8_t *data = nullptr;
        int size = 0;
        bool used = false;
    };
    const char *TAG = "OpenSLRender";

    const SLuint32 SL_QUEUE_BUFFER_COUNT = 2;

    //采样率
    SLuint32 m_pcm_sample_rate = SL_SAMPLINGRATE_48;
    //数据格式
    SLuint32 m_data_format = SL_DATAFORMAT_PCM;
    //声道数量
    SLuint32 m_channel_num = 2;

    SLObjectItf m_engine_obj = nullptr;
    SLEngineItf m_engine = nullptr;

    SLObjectItf m_output_mix_obj = nullptr;
    SLEnvironmentalReverbItf m_output_mix_evn_reverb = nullptr;
    SLEnvironmentalReverbSettings m_output_mix_evn_settings = SL_I3DL2_ENVIRONMENT_PRESET_DEFAULT;

    SLObjectItf m_pcm_player_obj = nullptr;
    SLPlayItf m_pcm_player = nullptr;
    SLVolumeItf m_pcm_volume = nullptr;

    SLAndroidSimpleBufferQueueItf m_pcm_buffer = nullptr;
    std::queue<PCMData *> m_data_queue;

    pthread_mutex_t m_cache_mutex = PTHREAD_MUTEX_INITIALIZER;
    pthread_cond_t m_cache_cond = PTHREAD_MUTEX_INITIALIZER;

    bool CreateEngine();

    bool CreateOutputMixer();

    bool ConfigPlayer();

    void StartRender();

    void BlockEnqueue();

    bool CheckError(SLresult result, std::string hint);

    void static sRenderPcm(OpenSLRender *that);

    void static sReadPcmBufferCbFun(SLAndroidSimpleBufferQueueItf bufferQueueItf, void *context);

    void WaitForCache() {
        pthread_mutex_lock(&m_cache_mutex);
        pthread_cond_wait(&m_cache_cond, &m_cache_mutex);
        pthread_mutex_unlock(&m_cache_mutex);
    }

    void SendCacheReadySignal() {
        pthread_mutex_lock(&m_cache_mutex);
        pthread_cond_signal(&m_cache_cond);
        pthread_mutex_unlock(&m_cache_mutex);
    }

public:
    OpenSLRender();

    ~OpenSLRender();

    void InitRender(AVStream *stream) override;

    void Render(uint8_t *pcm, int size) override;

    void ReleaseRender() override;

    const SLObjectItf_ * asInterface(SLObjectItf itf){
        return *itf;
    }

    const SLEngineItf_ * asInterface(SLEngineItf itf){
        return *itf;
    }
};


#endif //MULMEDIA_OPENSL_RENDER_H
