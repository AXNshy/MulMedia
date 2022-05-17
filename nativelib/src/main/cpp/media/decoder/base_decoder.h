//
// Created by ZhenqianXu on 2022/5/7.
//
#ifndef MULMEDIA_BASE_DECODER_H
#define MULMEDIA_BASE_DECODER_H


#include <jni.h>
#include <string.h>
#include <thread>


#include "../../utils/logger.h"
#include "decode_state.h"
#include "i_decoder.h"
#include <map>
using namespace std;
extern "C" {

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/frame.h"
#include "libavutil/time.h"
#include "libavutil/imgutils.h"
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

    // 为合成器提供解码
    bool m_for_synthesizer = false;

    void InitFFmpegDecoder(JNIEnv *env);

    void AllocFrameBuffer();

    void LoopDecode();

    void ObtainTimeStamp();

    void DoneDecode(JNIEnv *env);

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

    AVCodecParameters  *parameters;

    map<string,string> *metadata;

    void Wait(long second = 0);

    void SendSignal();

    virtual void Prepare(JNIEnv *env) = 0;


    virtual void Render(AVFrame *frame) = 0;

    virtual void Release() = 0;

    virtual AVMediaType GetMediaType() = 0;

    virtual bool NeedDecodeLoop() = 0;

    AVFrame* DecodeOneFrame();

    bool ForSynthesizer();
    /**
 * Log前缀
 */
    virtual const char *const LogSpec() = 0;

public:

    BaseDecoder(JNIEnv *env, jstring path, bool for_synthesizer = false);

    virtual ~BaseDecoder();

    int width() {
        return m_codec_ctx->width;
    }

    int height() {
        return m_codec_ctx->height;
    }

    AVPixelFormat video_pixel_format() {
        return m_codec_ctx->pix_fmt;
    }

    void Init(JNIEnv *pEnv, jstring pJstring);

    void GoOn() override;

    void Pause() override;

    void Stop() override;

    bool IsRunning() override;

    long GetDuration() override;

    long GetCurPos() override;

};


#endif //MULMEDIA_BASE_DECODER_H
