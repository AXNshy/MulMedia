//
// Created by 徐振乾 on 2023/7/3.
//

#ifndef MULMEDIA_CAMERAYUVSHADER_H
#define MULMEDIA_CAMERAYUVSHADER_H

#include "../IShader.h"

class CameraYUVShader : public IShader {
public:
    char *vertexShader();

    char *fragmentShader();
};

#endif //MULMEDIA_CAMERAYUVSHADER_H
