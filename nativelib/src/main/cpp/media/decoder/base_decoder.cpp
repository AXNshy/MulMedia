//
// Created by ZhenqianXu on 2022/5/7.
//

#include "base_decoder.h"
#include "../../utils/timer.c"


void BaseDecoder::CreateDecodeThread() {
    //创建一个智能指针，在线程结束时自动释放该指针对象
    std::shared_ptr<BaseDecoder> that(this);
    //创建一个线程  decode是创建线程后线程会执行的方法引用，that是传递给线程的参数解码器对象
    std::thread t(Decode, that);
    t.detach();
}

void BaseDecoder::InitFFmpegDecoder(JNIEnv *env) {
    //初始化上下文，AVFormat代表视频文件容器格式数据
    m_format_ctx = avformat_alloc_context();

    //打开文件
    if (avformat_open_input(&m_format_ctx, m_path, nullptr, nullptr) != 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to open media file [%s]", m_path);
        DoneDecode(env);
        return;
    }
    // 查询媒体流信息
    if (avformat_find_stream_info(m_format_ctx, nullptr) < 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to find media info [%s]", m_path);
        DoneDecode(env);
        return;
    }


    int vIdx = -1;
    //获取流索引
    for (int i = 0; i < m_format_ctx->nb_streams; ++i) {
        if (m_format_ctx->streams[i]->codecpar->codec_type == GetMediaType()) {
            vIdx = i;
            break;
        }
    }

    if (vIdx == -1) {
        LOG_ERROR(TAG, LogSpec(), "fail to find video stream index [%s]", m_path);
        DoneDecode(env);
        return;
    }

    m_stream_index = vIdx;

    LOGD(TAG, "av_dict_get");
    const AVDictionaryEntry *m = nullptr;
    while ((m = av_dict_get(m_format_ctx->streams[m_stream_index]->metadata, "", m, AV_DICT_IGNORE_SUFFIX)) !=
            nullptr) {
        LOGD(TAG, "metadata key:%s,value:%s", m->key, m->value);
    }

    //获取解码器参数
    parameters = m_format_ctx->streams[m_stream_index]->codecpar;
    // 搜索对应解码器
    m_codec = avcodec_find_decoder(parameters->codec_id);

    m_codec_ctx = avcodec_alloc_context3(m_codec);
    // 初始化解码器上下文
    if (avcodec_parameters_to_context(m_codec_ctx, parameters) != 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to initial codec context,[%s]", m_path);
        DoneDecode(env);
        return;
    }
    // 打开解码器
    if (avcodec_open2(m_codec_ctx, m_codec, nullptr) < 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to open codec [%s]", m_path);
        DoneDecode(env);
        return;
    }
    // 获取视频时长
    m_dur = (long) ((float) m_format_ctx->duration / AV_TIME_BASE * 1000);


    LOGD(TAG, "decoder init Success [%s]", m_path);
}

void BaseDecoder::AllocFrameBuffer() {
    m_packet = av_packet_alloc();
    m_frame = av_frame_alloc();
}

void BaseDecoder::LoopDecode() {
    if (STOP == m_state) {
        m_state = START;
    }

    LOG_INFO(TAG, LogSpec(), "start decode llllllloop ,start state is [%d]" , m_state);

    while (1) {

        LOG_INFO(TAG, LogSpec(), "m_state %d", m_state);
        if (m_state != START &&
            m_state != DECODING &&
            m_state != STOP) {
            Wait();
            m_start_t = GetCurMsTime() - m_cur_t_s;
        }
        if (m_state == STOP) {
            break;
        }

        if (m_start_t == -1) {
            m_start_t = GetCurMsTime();
        }

        if (DecodeOneFrame() != nullptr) {
            SyncRender();
            LOGD(TAG,"Render pts:%lld" ,m_frame->pts);
            Render(m_frame);
            if (m_state == START) {
                m_state = PAUSE;
            }
        } else {
            if (ForSynthesizer()) {
                m_state = STOP;
            } else {
                m_state = FINISH;
            }
        }
    }
}

void BaseDecoder::ObtainTimeStamp() {
    if (m_frame->pkt_dts != AV_NOPTS_VALUE) {
        m_cur_t_s = m_packet->dts;
    } else if (m_frame->pts != AV_NOPTS_VALUE) {
        m_cur_t_s = m_frame->pts;
    } else {
        m_cur_t_s = 0;
    }
    m_cur_t_s = (int64_t) ((m_cur_t_s * av_q2d(m_format_ctx->streams[m_stream_index]->time_base)) *
                           1000);
}

void BaseDecoder::DoneDecode(JNIEnv *env) {
    if (m_packet != nullptr) {
        av_packet_free(&m_packet);
    }
    if (m_frame != nullptr) {
        av_frame_free(&m_frame);
    }

    if (m_codec_ctx != nullptr) {
        avcodec_close(m_codec_ctx);
        avcodec_free_context(&m_codec_ctx);
    }

    if (m_format_ctx != nullptr) {
        avformat_close_input(&m_format_ctx);
        avformat_free_context(m_format_ctx);
    }

    if (m_path != nullptr && m_path_ref != nullptr) {
        env->ReleaseStringUTFChars(static_cast<jstring>(m_path_ref), m_path);
        env->DeleteGlobalRef(m_path_ref);
    }

    Release();
}

void BaseDecoder::SyncRender() {

}

//解码线程运行代码
void BaseDecoder::Decode(std::shared_ptr<BaseDecoder> that) {
    //新的线程也需要一个新的JNIEnv指针，用来与JVM通信
    JNIEnv *env;

    if (that->m_jvm_for_thread->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        LOG_ERROR(that->TAG, that->LogSpec(), "fail to init decode thread")
        return;
    }
    //初始化解码器
    that->InitFFmpegDecoder(env);
    // 为帧缓冲分配内存
    that->AllocFrameBuffer();
    //回调通知解码器初始化完成

    av_usleep(2000);
    that->Prepare(env);
    // 进入解码循环
    that->LoopDecode();
    // 退出解码
    that->DoneDecode(env);
    // 线程结束，JavaVM解除与该线程关联
    that->m_jvm_for_thread->DetachCurrentThread();
}

void BaseDecoder::Wait(long second) {
    LOGD(LogSpec(), "decoder Wait second:%d", second)
    pthread_mutex_lock(&m_mutex);
    if (second > 0) {
        timeval now;
        timespec outtime;
        gettimeofday(&now, NULL);
        int64_t destNSec = now.tv_usec * 1000;
        outtime.tv_sec = static_cast<__kernel_time_t>(now.tv_sec + second + destNSec / 1000000000);
        outtime.tv_nsec = static_cast<long>(destNSec % 1000000000);
        pthread_cond_timedwait(&m_cond, &m_mutex, &outtime);
    } else {
        pthread_cond_wait(&m_cond, &m_mutex);
    }
    pthread_mutex_unlock(&m_mutex);

}

void BaseDecoder::SendSignal() {
    LOGD(TAG, "decoder SendSignal")
    pthread_mutex_lock(&m_mutex);
    pthread_cond_signal(&m_cond);
    pthread_mutex_unlock(&m_mutex);
}

BaseDecoder::BaseDecoder(JNIEnv *env, jstring path, bool for_synthesizer) : m_for_synthesizer(
        for_synthesizer) {
    Init(env, path);
    CreateDecodeThread();
}

BaseDecoder::~BaseDecoder() {
    if (m_format_ctx != nullptr) delete m_format_ctx;
    if (m_codec_ctx != nullptr) delete m_codec_ctx;
    if (m_packet != nullptr) delete m_packet;
    if (m_frame != nullptr) delete m_frame;
}

void BaseDecoder::Init(JNIEnv *env, jstring path) {
    m_path_ref = env->NewGlobalRef(path);
    m_path = env->GetStringUTFChars(path, nullptr);
    env->GetJavaVM(&m_jvm_for_thread);
    LOGD(TAG, "decoder create path:%s", m_path)
}

AVFrame *BaseDecoder::DecodeOneFrame() {

//    LOG(TAG, LogSpec(), "DecodeOneFrame %d", m_state);
    int ret = av_read_frame(m_format_ctx, m_packet);

    while (ret == 0) {
        if (m_packet->stream_index == m_stream_index) {

            switch (avcodec_send_packet(m_codec_ctx, m_packet)) {
                //数据阻塞了，需要从解码器输出中将数据清掉，才能继续给解码器提供数据
                case AVERROR(EAGAIN) :
                    av_packet_unref(m_packet);
                    LOG_ERROR(TAG, LogSpec(), "avcodec send packet error [%s]",
                              av_err2str(AVERROR(EAGAIN)))
                    break;
                    //解码完成
                case AVERROR_EOF:
                    LOG_ERROR(TAG, LogSpec(), "avcodec work  complete [%s]",
                              av_err2str(AVERROR_EOF))
                    return nullptr;
                    //解码器没有打开，codec是编码模式，或者需要把当前数据flush掉。
                case AVERROR(EINVAL):
                    LOG_ERROR(TAG, LogSpec(), "decoder not open,or it's encoder, [%s]",
                              av_err2str(AVERROR(EINVAL)))
                    break;
                case AVERROR(ENOMEM):
                    LOG_ERROR(TAG, LogSpec(), "decoder error, [%s]", av_err2str(AVERROR(EINVAL)))
                    break;

                default:
                    break;
            }
            int result = avcodec_receive_frame(m_codec_ctx, m_frame);
            switch (result) {
                case 0:
                    ObtainTimeStamp();
                    av_packet_unref(m_packet);
                    return m_frame;
                default:
                    av_packet_unref(m_packet);
                    LOG_ERROR(TAG, LogSpec(), "avcodec_receive_frame error [%s]",
                              av_err2str(result))
                    break;
            }
        }
        av_packet_unref(m_packet);
        ret = av_read_frame(m_format_ctx, m_packet);
    }

    av_packet_unref(m_packet);
    return nullptr;
}

bool BaseDecoder::ForSynthesizer() {
    return false;
}

void BaseDecoder::GoOn() {
    m_state = DECODING;
    SendSignal();

}

void BaseDecoder::Pause() {
    m_state = PAUSE;
}

void BaseDecoder::Stop() {
    m_state = STOP;
}

bool BaseDecoder::IsRunning() {
    return m_state == DECODING;
}

long BaseDecoder::GetDuration() {
    return m_dur;
}

long BaseDecoder::GetCurPos() {
    return m_cur_t_s;
}

void BaseDecoder::CallbackState(DecodeState status) {
    if (m_state_cb != NULL) {
        switch (status) {
            case PREPARE:
                m_state_cb->DecodePrepare(this);
                break;
            case START:
                m_state_cb->DecodeReady(this);
                break;
            case DECODING:
                m_state_cb->DecodeRunning(this);
                break;
            case PAUSE:
                m_state_cb->DecodePause(this);
                break;
            case FINISH:
                m_state_cb->DecodeFinish(this);
                break;
            case STOP:
                m_state_cb->DecodeStop(this);
                break;
        }
    }
}








