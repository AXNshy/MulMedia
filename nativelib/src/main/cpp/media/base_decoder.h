//
// Created by ZhenqianXu on 2022/5/7.
//
#ifndef MULMEDIA_BASE_DECODER_H
#define MULMEDIA_BASE_DECODER_H


#include <jni.h>
#include <string>
#include <thread>


#include "../utils/logger.h"
#include "decode_state.h"
#include "i_decoder.h"


extern "C" {

#include "../include/libavcodec/avcodec.h"
#include "../include/libavformat/avformat.h"
#include "../include/libavutil/frame.h"
#include "../include/libavutil/time.h"
}

class BaseDecoder : public IDecoder {
private:
    const char *TAG = "BaseDecoder";

    AVFormatContext *m_format_ctx = nullptr;

    const AVCodec *m_codec = nullptr;

    AVCodecContext *m_codec_ctx = nullptr;

    AVPacket *m_packet = nullptr;

    AVFrame *m_frame = nullptr;

    int64_t m_cur_t_s = 0;

    long m_dur = 0;

    int64_t m_start_t = -1;

    DecodeState m_state = STOP;

    int m_stream_index = -1;


    void InitFFmpegDecoder(JNIEnv *env);

    void AllocFrameBuffer();

    void LoopDecode();

    void ObtainTimeStamp();

    void DoneDecode();

    void SyncRender();


    JavaVM *m_jvm_for_thread;
    //jstring不是标准的c++类型，转换成char*类型方便使用
    jobject m_path_ref = NULL;

    const char *m_path = NULL;

    pthread_mutex_t m_mutex = PTHREAD_MUTEX_INITIALIZER;

    pthread_cond_t m_cond = PTHREAD_COND_INITIALIZER;

    void CreateDecodeThread();

    static void Decode(std::shared_ptr<BaseDecoder> that);

protected:

    void Wait(long second = 0);

    void SendSignal();

    virtual void Prepare(JNIEnv *env) = 0;

    virtual void Render(AVFrame *frame) = 0;

    virtual void Release() = 0;

    virtual AVMediaType GetMediaType() = 0;

    void *DecodeOneFrame();

    bool ForSynthesizer();
    /**
 * Log前缀
 */
    virtual const char *const LogSpec() = 0;

public:

    BaseDecoder(JNIEnv *env, jstring path);

    virtual ~BaseDecoder();

    void Init(JNIEnv *pEnv, jstring pJstring);

};


#endif //MULMEDIA_BASE_DECODER_H
