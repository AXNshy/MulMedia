//
// Created by Luffy on 2023/4/17.
//
#ifndef GL_GLEXT_PROTOTYPES
#define GL_GLEXT_PROTOTYPES
#endif
#ifndef EGL_NATIVE_BUFFER_ANDROID 0x3140
#define EGL_NATIVE_BUFFER_ANDROID 0x3140
#endif
#ifndef EGL_IMAGE_PRESERVED_KHR   0x30D2
#define EGL_IMAGE_PRESERVED_KHR   0x30D2
#endif
#ifndef EGL_EGLEXT_PROTOTYPES
#define EGL_EGLEXT_PROTOTYPES
#endif

#ifndef MULMEDIA_IDRAWER_H
#define MULMEDIA_IDRAWER_H

#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>

#include "stdlib.h"

class IDrawer {
public:
    IDrawer();

    ~IDrawer();

    void init();

    void initProgram();

    virtual void initParams() = 0;

    void release();

    virtual void draw() = 0;

    void setShader(char *vShaderStr, char *fShaderStr);

    void setViewSize(int width, int height);

    void setEGLImage(EGLImageKHR eglImageKhr);

protected:
    GLuint programId = 0;

    int width = 0;

    int height = 0;

    char *vShaderStr = nullptr;
    char *fShaderStr = nullptr;

    EGLImageKHR eglImageKhr = NULL;

    GLuint LoadShader(GLenum type, const char *shaderStr);

    virtual const char *getDrawerType() = 0;
};


#endif //MULMEDIA_IDRAWER_H
