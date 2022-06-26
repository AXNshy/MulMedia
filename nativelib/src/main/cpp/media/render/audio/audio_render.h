//
// Created by ZhenqianXu on 2022/5/21.
//

#ifndef MULMEDIA_AUDIO_RENDER_H
#define MULMEDIA_AUDIO_RENDER_H

#include <cstdint>
#include <libavformat/avformat.h>

class AudioRender {
public:
    virtual void InitRender(AVStream *stream) = 0;
    virtual void Render(uint8_t *pcm, int size) = 0;
    virtual void ReleaseRender() = 0;
    virtual ~AudioRender() {}
};

#endif //MULMEDIA_AUDIO_RENDER_H
