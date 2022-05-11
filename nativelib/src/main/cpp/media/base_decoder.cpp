//
// Created by ZhenqianXu on 2022/5/7.
//

#include "base_decoder.h"
#include "../utils/timer.c"


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
    if (avformat_open_input(&m_format_ctx, m_path, NULL, NULL) != 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to open media file [%s]", m_path);
        DoneDecode();
        return;
    }
    // 查询媒体流信息
    if (avformat_find_stream_info(m_format_ctx, NULL) < 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to find media info [%s]", m_path);
        DoneDecode();
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
        DoneDecode();
        return;
    }

    m_stream_index = vIdx;
    //获取解码器参数
    AVCodecParameters *parameters = m_format_ctx->streams[m_stream_index]->codecpar;
    // 搜索对应解码器
    m_codec = avcodec_find_decoder(parameters->codec_id);
    // 初始化解码器上下文
    if (avcodec_parameters_to_context(m_codec_ctx, parameters) != 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to initial codec context,[%s]", m_path);
        DoneDecode();
        return;
    }
    // 打开解码器
    if (avcodec_open2(m_codec_ctx, m_codec, NULL) < 0) {
        LOG_ERROR(TAG, LogSpec(), "fail to open codec [%s]", m_path);
        DoneDecode();
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

    LOG_INFO(TAG, LogSpec(), "start decode llllllloop");

    while (1) {

        if (m_state != START ||
            m_state != DECODING ||
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
            Render(m_frame);
            if (m_state == START) {
                m_state = PAUSE;
            }
        } else {
            if(ForSynthesizer()){
                m_state = STOP;
            } else{
                m_state = FINISH;
            }
        }

    }
}

void BaseDecoder::ObtainTimeStamp() {

}

void BaseDecoder::DoneDecode() {

}

void BaseDecoder::SyncRender() {

}

//解码线程运行代码
void BaseDecoder::Decode(std::shared_ptr<BaseDecoder> that) {
    //新的线程也需要一个新的JNIEnv指针，用来与JVM通信
    JNIEnv *env;

    if (that->m_jvm_for_thread->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOG_ERROR(that->TAG, that->LogSpec(), "fail to init decode thread")
        return;
    }
    //初始化解码器
    that->InitFFmpegDecoder(env);
    // 为帧缓冲分配内存
    that->AllocFrameBuffer();
    //回调通知解码器初始化完成
    that->Prepare(env);
    // 进入解码循环
    that->LoopDecode();
    // 退出解码
    that->DoneDecode();
    // 线程结束，JavaVM解除与该线程关联
    that->m_jvm_for_thread->DetachCurrentThread();
}

void BaseDecoder::Wait(long second) {

}

void BaseDecoder::SendSignal() {

}

BaseDecoder::BaseDecoder(JNIEnv *env, jstring path) {
    Init(env, path);
    CreateDecodeThread();
}

BaseDecoder::~BaseDecoder() {
    if (m_format_ctx != NULL) delete m_format_ctx;
    if (m_codec_ctx != NULL) delete m_codec_ctx;
    if (m_packet != NULL) delete m_packet;
    if (m_frame != NULL) delete m_frame;
}

void BaseDecoder::Init(JNIEnv *env, jstring path) {
    m_path_ref = env->NewGlobalRef(path);
    m_path = env->GetStringUTFChars(path, NULL);
    env->GetJavaVM(&m_jvm_for_thread);
}

void *BaseDecoder::DecodeOneFrame() {
    return nullptr;
}

bool BaseDecoder::ForSynthesizer() {
    return false;
}
