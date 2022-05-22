//
// Created by ZhenqianXu on 2022/5/23.
//

#ifndef MULMEDIA_I_DECODE_STATE_CB_H
#define MULMEDIA_I_DECODE_STATE_CB_H

class IDecoder;

class IDecodeCallback {
public:
    IDecodeCallback();
    virtual void DecodePrepare(IDecoder *decoder) = 0;
    virtual void DecodeReady(IDecoder *decoder) = 0;
    virtual void DecodeRunning(IDecoder *decoder) = 0;
    virtual void DecodePause(IDecoder *decoder) = 0;
    virtual void DecodeOnFrame(IDecoder *decoder) = 0;
    virtual void DecodeFinish(IDecoder *decoder) = 0;
    virtual void DecodeStop(IDecoder *decoder) = 0;
};


#endif //MULMEDIA_I_DECODE_STATE_CB_H
