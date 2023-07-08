//
// Created by 徐振乾 on 2023/6/30.
//

#ifndef MULMEDIA_ISHADER_H
#define MULMEDIA_ISHADER_H

class IShader {
public:
    virtual char *vertexShader() = 0;

    virtual char *fragmentShader() = 0;
};

#endif //MULMEDIA_ISHADER_H
