//
// Created by ZhenqianXu on 2022/5/15.
//

#ifndef MULMEDIA_ONE_FRAME_H
#define MULMEDIA_ONE_FRAME_H

#include <malloc.h>
#include "../utils/logger.h"

extern "C"{
    #include "../include/libavutil/rational.h"
};

class OneFrame {
public:
    //数据缓冲
    uint8_t *data = NULL;
    //行数据大小
    int line_size;
    //显示时间戳
    int64_t pts;

    AVRational time_base;

    uint8_t *ext_data = NULL;

    // 是否自动回收data和ext_data
    bool autoRecycle = true;

    OneFrame(uint8_t *data, int line_size, int64_t pts, AVRational time_base,
             uint8_t *ext_data = NULL, bool autoRecycle = true) {
        this->data = data;
        this->line_size = line_size;
        this->pts = pts;
        this->time_base = time_base;
        this->ext_data = ext_data;
        this->autoRecycle = autoRecycle;
    }

    ~OneFrame() {
        if (autoRecycle) {
            if (data != NULL) {
                free(data);
                data = NULL;
            }
            if (ext_data != NULL) {
                free(ext_data);
                ext_data = NULL;
            }
        }
    }
};

#endif //MULMEDIA_ONE_FRAME_H
