//
// Created by 徐振乾 on 2023/6/30.
//



#ifndef MULMEDIA_TEXTUREDRAWER_H
#define MULMEDIA_TEXTUREDRAWER_H


#include "../IDrawer.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2ext.h>
#include <GLES3/gl3.h>
#include <android/hardware_buffer.h>


class TextureDrawer : public IDrawer {
public:
    TextureDrawer();

    ~TextureDrawer();

    void release();

    virtual void draw() override;

    void initParams() override;

    void updateImageBuffer(AHardwareBuffer *buffer);

private:
    const char *TAG = "TextureDrawer";

    GLint y_texture_id = -1;
    int y_texture_width = 0;
    int y_texture_height = 0;

    GLint u_texture_id = -1;
    int u_texture_width = 0;
    int u_texture_height = 0;

    GLint v_texture_id = -1;
    int v_texture_width = 0;
    int v_texture_height = 0;

    GLint mvp_matrix_id = -1;
    float mvpMatrix[16];

    GLuint texId = -1;

    GLint oesTexture = 0;

protected:
    const char *getDrawerType() override;
};


#endif //MULMEDIA_TEXTUREDRAWER_H
