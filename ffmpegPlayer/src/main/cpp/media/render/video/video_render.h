//
// Created by ZhenqianXu on 2022/5/14.
//

#ifndef MULMEDIA_VIDEO_RENDER_H
#define MULMEDIA_VIDEO_RENDER_H


#include <jni.h>
#include "../../one_frame.h"


class VideoRender{
public:
    virtual void InitRender(JNIEnv *env,int video_width,int video_height,int *dst_size) = 0;

    virtual void Render(OneFrame *frame) = 0;

    virtual void ReleaseRender() = 0;
};

#endif MULMEDIA_VIDEO_RENDER_H
