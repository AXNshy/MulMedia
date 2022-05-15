//
// Created by ZhenqianXu on 2022/5/7.
//

#ifndef MULMEDIA_I_DECODER_H
#define MULMEDIA_I_DECODER_H

class IDecoder{
public:
    //继续解码
    virtual void GoOn() = 0;
    //暂停解码
    virtual void Pause() = 0;
    //停止解码
    virtual void Stop() = 0;
    //正在运行中
    virtual bool IsRunning() = 0;
    //获取视频长度
    virtual long GetDuration() = 0;
    //获取当前解码位置
    virtual long GetCurPos() = 0;

};

#endif //MULMEDIA_I_DECODER_H
