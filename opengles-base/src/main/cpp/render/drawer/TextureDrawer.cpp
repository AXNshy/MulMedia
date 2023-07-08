//
// Created by 徐振乾 on 2023/6/30.
//


#include "TextureDrawer.h"
#include "../../utils/logger.h"


void TextureDrawer::release() {
    if (programId > 0) {
        glDeleteProgram(programId);
    }
}

void TextureDrawer::draw() {
    LOGD(getDrawerType(), "draw programId:%d", programId);
    if (programId == 0) {
        init();
    }

    GLfloat vVertexData[] = {
            -1.0, -1.0, 0.0,
            -1.0, 1.0, 0.0,
            1.0, -1.0, 0.0,
            1.0, 1.0, 0.0
    };
    GLfloat vTextureData[] = {
            0.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            1.0, 0.0, 0.0,
            1.0, 1.0, 0.0
    };

    glClearColor(1.0, 1.0, 1.0, 1.0);
    glUseProgram(programId);

    glGenTextures(1, &texId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, texId);
    glUniform1i(oesTexture, 0);
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


    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    glUniformMatrix4fv(mvp_matrix_id, 1, false, mvpMatrix);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, vVertexData);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, vTextureData);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
    LOGD(getDrawerType(), "draw texture")
}

void TextureDrawer::initParams() {
    mvp_matrix_id = glGetUniformLocation(programId, "u_MVPMatrix");
    oesTexture = glGetUniformLocation(programId, "s_texture");
}

TextureDrawer::~TextureDrawer() = default;

const char *TextureDrawer::getDrawerType() {
    return TAG;
}

void TextureDrawer::updateImageBuffer(AHardwareBuffer *buffer) {

}

TextureDrawer::TextureDrawer() = default;
