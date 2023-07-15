//
// Created by 徐振乾 on 2023/6/30.
//


#include "TextureDrawer.h"
#include "../../utils/logger.h"


void TextureDrawer::release() {
    if (programId > 0) {
        glDeleteProgram(programId);
        programId = 0;
    }
}

void TextureDrawer::draw() {
    LOGD(getDrawerType(), "draw programId:%d", programId);
    if (programId <= 0) {
        LOGD(getDrawerType(), "programId is not available");
        return;
    }

    GLfloat vVertexData[] = {
            -1.0, -1.0, 0.0,
            -1.0, 1.0, 0.0,
            1.0, -1.0, 0.0,
            1.0, 1.0, 0.0
    };
    GLfloat vTextureData[] = {
            0.0, 0.0,
            0.0, 1.0,
            1.0, 0.0,
            1.0, 1.0,
    };

    glUseProgram(programId);
    getGLError("glUseProgram");

    glClearColor(1.0, 1.0, 1.0, 1.0);
    getGLError("glClearColor");
    glActiveTexture(GL_TEXTURE0);
    getGLError("glActiveTexture GL_TEXTURE0");
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, texId);
    getGLError("glBindTexture GL_TEXTURE_EXTERNAL_OES");
    glUniform1i(oesTexture, 0);
    getGLError("glUniform1i oesTexture");
    glTexParameterf(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
    );
    glTexParameterf(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
    );
    glTexParameterf(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_EDGE
    );
    glTexParameterf(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_EDGE
    );

    LOGD(getDrawerType(), "draw texId:%d", texId)

    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, eglImageKhr);
    getGLError("glEGLImageTargetTexture2DOES");
//
//    GLint defaultFramebuffer = 0;
//    glGetIntegerv ( GL_FRAMEBUFFER_BINDING, &defaultFramebuffer );
//
//    glBindFramebuffer ( GL_FRAMEBUFFER, Tmp_Framebuffer );
//    // textureId:  要读取的TextureID
//    glFramebufferTexture2D ( GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES,
//                             oesTexture, 0 );
//
//    //默认读取GL_COLOR_ATTACHMENT0，此处可以不设置
//    glReadBuffer ( GL_COLOR_ATTACHMENT0 );
//
//
//    GLint readType, readFormat;
//
//    glGetIntegerv ( GL_IMPLEMENTATION_COLOR_READ_TYPE, &readType );
//    glGetIntegerv ( GL_IMPLEMENTATION_COLOR_READ_FORMAT, &readFormat );
//    LOGD(getDrawerType(), "draw readType:%d", readType);
//    LOGD(getDrawerType(), "draw readFormat:%d", readFormat);
//
//    GLubyte *pixels;
//    pixels = (GLubyte*) malloc( 1920 * 1080 * 4 );
//    glReadPixels(0,0,1920,1080,GL_RGB,GL_INT,pixels);
//
//
//    glBindFramebuffer ( GL_FRAMEBUFFER, defaultFramebuffer );

    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    glUniformMatrix4fv(mvp_matrix_id, 1, false, mvpMatrix);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, vVertexData);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, vTextureData);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
    LOGD(getDrawerType(), "draw texture")
}


void TextureDrawer::initParams() {
    mvp_matrix_id = glGetUniformLocation(programId, "u_MVPMatrix");
    oesTexture = glGetUniformLocation(programId, "s_texture");

//    glGenFramebuffers ( 1, &Tmp_Framebuffer );

    glGenTextures(1, &texId);
}

TextureDrawer::~TextureDrawer() = default;

const char *TextureDrawer::getDrawerType() {
    return TAG;
}

void TextureDrawer::updateImageBuffer(AHardwareBuffer *buffer) {

}

TextureDrawer::TextureDrawer() {
}

void TextureDrawer::getGLError(const char *message) {
    GLenum error = glGetError();
    if (error != GL_NO_ERROR) {
        LOGE(getDrawerType(), "%s getError is %d", message, error);
    }
}
