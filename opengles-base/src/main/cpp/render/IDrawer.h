//
// Created by Luffy on 2023/4/17.
//

#ifndef MULMEDIA_IDRAWER_H
#define MULMEDIA_IDRAWER_H


#include "IShader.h"

class IDrawer {
public:
    void release();

    void draw();

    void setShader(IShader shader);
};


#endif //MULMEDIA_IDRAWER_H
