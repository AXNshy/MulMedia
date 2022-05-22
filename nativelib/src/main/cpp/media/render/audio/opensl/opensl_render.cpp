//
// Created by ZhenqianXu on 2022/5/21.
//

#include <thread>
#include <unistd.h>
#include "opensl_render.h"

#include "../../../../utils/logger.h"

/*
 * Create and Initialize Engine
 * */
bool OpenSLRender::CreateEngine() {
    SLresult lresult =slCreateEngine(&m_engine_obj,0, nullptr,0, nullptr, nullptr);
    if(CheckError(lresult, "Engine")) return false;
    lresult = asInterface(m_engine_obj)->Realize(m_engine_obj,SL_BOOLEAN_FALSE);
    if(CheckError(lresult,"EngineRealize")) return false;

    lresult = asInterface(m_engine_obj)->GetInterface(m_engine_obj,SL_IID_ENGINE,&m_engine);
    if(CheckError(lresult,"GetInterface")) return false;

    return true;
}

/*
 * initialize Mixer
 * */
bool OpenSLRender::CreateOutputMixer() {
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};

    SLresult lresult = asInterface(m_engine)->CreateOutputMix(m_engine,&m_output_mix_obj,1,mids,mreq);

    if(CheckError(lresult,"Output Mix")) return false;

    lresult = asInterface(m_output_mix_obj)->Realize(m_output_mix_obj,SL_BOOLEAN_FALSE);
    if(CheckError(lresult,"Mix Realize")) return false;

    lresult = asInterface(m_output_mix_obj)->GetInterface(m_output_mix_obj,SL_IID_ENVIRONMENTALREVERB,&m_output_mix_evn_reverb);
    if(CheckError(lresult,"Mix Env Reverb")) return false;

    if(lresult == SL_RESULT_SUCCESS){
        (*m_output_mix_evn_reverb)->SetEnvironmentalReverbProperties(m_output_mix_evn_reverb,&m_output_mix_evn_settings);
    }
    return true;
}

/*
 * Initialize and Configure Player
 *
 * 1.CraeteAudioPlayer()
 *
 * */
bool OpenSLRender::ConfigPlayer() {
    SLDataLocator_AndroidSimpleBufferQueue android_queue ={SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,SL_QUEUE_BUFFER_COUNT};
    SLDataFormat_PCM pcm = {
SL_DATAFORMAT_PCM,
(SLuint32)2,
SL_SAMPLINGRATE_44_1,
SL_PCMSAMPLEFORMAT_FIXED_16,
SL_PCMSAMPLEFORMAT_FIXED_16,
SL_SPEAKER_FRONT_LEFT|SL_SPEAKER_FRONT_RIGHT,
SL_BYTEORDER_LITTLEENDIAN
    };
    SLDataSource slDataSource ={&android_queue,&pcm};
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,m_output_mix_obj};
    SLDataSink dataSink = {&outputMix, nullptr};


    SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE,SL_IID_EFFECTSEND,SL_IID_VOLUME};
    SLboolean reqs[3] = {SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,};

    SLresult  lresult = asInterface(m_engine)->CreateAudioPlayer(m_engine,&m_pcm_player_obj,&slDataSource,&dataSink,3,ids,reqs);
    if(CheckError(lresult,"Create Audio Player")) return false;

    lresult = asInterface(m_pcm_player_obj)->Realize(m_pcm_player_obj,SL_BOOLEAN_FALSE);
    if(CheckError(lresult,"Player Realize")) return false;

    lresult = asInterface(m_pcm_player_obj)->GetInterface(m_pcm_player_obj,SL_IID_PLAY,&m_pcm_player);
    if(CheckError(lresult,"Player GetInterface")) return false;


    lresult = asInterface(m_pcm_player_obj)->GetInterface(m_pcm_player_obj,SL_IID_BUFFERQUEUE,&m_pcm_buffer);
    if(CheckError(lresult,"Buffer GetInterface")) return false;

    lresult = (*m_pcm_buffer)->RegisterCallback(m_pcm_buffer,sReadPcmBufferCbFun,this);
    if(CheckError(lresult,"Buffer RegisterCallback")) return false;

    lresult = asInterface(m_pcm_player_obj)->GetInterface(m_pcm_player_obj,SL_IID_VOLUME,&m_pcm_volume);
    if(CheckError(lresult,"Player Volume  GetInterface")) return false;

    LOGD(TAG,"OpenSL ES init success");
    return true;
}

void OpenSLRender::StartRender() {
    while (m_data_queue.empty()){
        WaitForCache();
    }

    (*m_pcm_player)->SetPlayState(m_pcm_player,SL_PLAYSTATE_PLAYING);
    sReadPcmBufferCbFun(m_pcm_buffer,this);
}

void OpenSLRender::BlockEnqueue() {
    LOGD(TAG,"BlockEnqueue")
    if(m_pcm_player == nullptr) return;

    while (!m_data_queue.empty()){
        PCMData *front = m_data_queue.front();
        if(front->used){
            m_data_queue.pop();
            delete front;
        } else{
            break;
        }
    }
    while (m_data_queue.empty() && m_pcm_player != nullptr){
        WaitForCache();
    }

    PCMData *front = m_data_queue.front();
    if(front != nullptr && m_pcm_player){
        SLresult  lresult = (*m_pcm_buffer)->Enqueue(m_pcm_buffer,front->data,front->size);
        if(lresult == SL_RESULT_SUCCESS) {
            front->used = true;
        }else{
            LOGD(TAG,"Buffer Enqueue Fail");
        }
    }
}

bool OpenSLRender::CheckError(SLresult result, string hint) {
    if(result != SL_RESULT_SUCCESS){
        LOGE(TAG,"OpenSL ES [%s] init fail",hint.c_str());
        return true;
    }
    return false;
}

void OpenSLRender::sRenderPcm(OpenSLRender *that) {
    LOGD(that->TAG,"sRenderPcm");
    that->StartRender();
}

void
OpenSLRender::sReadPcmBufferCbFun(SLAndroidSimpleBufferQueueItf bufferQueueItf, void *context) {
    OpenSLRender *render = static_cast<OpenSLRender *>(context);
    render->BlockEnqueue();
}

OpenSLRender::OpenSLRender() {

}

OpenSLRender::~OpenSLRender() {

}

/*
 * Create Engine
 * Create Mixer
 * Config Player
 * */
void OpenSLRender::InitRender() {
    LOGD(TAG,"InitRender")
    if (!CreateEngine()) return;
    if (!CreateOutputMixer()) return;
    if (!ConfigPlayer()) return;

    thread t(sRenderPcm, this);
    t.detach();
    LOGD(TAG,"thread detach")
}

void OpenSLRender::Render(uint8_t *pcm, int size) {
    LOGD(TAG,"Render")
    if(m_pcm_player != nullptr){
        if(pcm != nullptr && size > 0){
            while (m_data_queue.size() >= 2){
                SendCacheReadySignal();
                usleep(20000);
            }

            uint8_t *data = static_cast<uint8_t *>(malloc(size));
            memcpy(data,pcm,size);
            PCMData *newData = new PCMData(data,size);
            m_data_queue.push(newData);
            SendCacheReadySignal();
        }
    }else{
        free(pcm);
    }
}

/*
 * 1.Set SL Player State to STOP
 * 2.Destory Player
 * 3.Destory Mixer
 * 4.Destory Engine
 * */
void OpenSLRender::ReleaseRender() {
    if(m_pcm_player != nullptr){
        (*m_pcm_player)->SetPlayState(m_pcm_player,SL_PLAYSTATE_STOPPED);
        m_pcm_player = nullptr;
    }

    SendCacheReadySignal();

    if(m_pcm_player_obj != nullptr){
        asInterface(m_pcm_player_obj)->Destroy(m_pcm_player_obj);
        m_pcm_player_obj = nullptr;
        m_pcm_buffer = nullptr;
        m_pcm_volume = nullptr;
    }

    if(m_output_mix_obj != nullptr){
        asInterface(m_output_mix_obj)->Destroy(m_output_mix_obj);
        m_output_mix_obj = nullptr;
        m_output_mix_evn_reverb = nullptr;
    }

    if(m_engine_obj != nullptr){
        asInterface(m_engine_obj)->Destroy(m_engine_obj);
        m_engine_obj = nullptr;
        m_engine = nullptr;
    }

    while (!m_data_queue.empty()){
        PCMData *front = m_data_queue.front();
        m_data_queue.pop();
        delete front;
    }


}
