//
// Created by 徐振乾 on 2023/7/1.
//
#include "IDrawer.h"
#include "../utils/logger.h"

GLuint IDrawer::LoadShader(GLenum type, const char *shaderStr) {
    GLuint shader;
    GLint compiled;
    shader = glCreateShader(type);
    if (shader == 0) return 0;
    glShaderSource(shader, 1, &shaderStr, NULL);
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        if (infoLen > 1) {
            char *infoLog = static_cast<char *>(malloc(sizeof(char) * infoLen));
            glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
            LOGE(getDrawerType(), "Error compiling shader:\n%s\n", infoLog)
            free(infoLog);
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

void IDrawer::setShader(IShader *shader) {
    shaderFactory = shader;
    vShaderStr = shaderFactory->vertexShader();
    fShaderStr = shaderFactory->fragmentShader();
}

void IDrawer::init() {
    LOGD(getDrawerType(), "init")
    initProgram();
    initParams();
}

void IDrawer::initProgram() {
    if (programId == 0) {
        programId = glCreateProgram();
        LOGD(getDrawerType(), "initProgram %d", programId);
        if (programId == 0) {
            return;
        }
        GLuint shaderVertexId = LoadShader(GL_VERTEX_SHADER, vShaderStr);
        GLuint shaderFragId = LoadShader(GL_FRAGMENT_SHADER, fShaderStr);

        glAttachShader(programId, shaderVertexId);
        glAttachShader(programId, shaderFragId);

        glLinkProgram(programId);

        GLint linked;

        glGetProgramiv(programId, GL_LINK_STATUS, &linked);
        if (!linked) {
            GLint infoLen = 0;
            glGetProgramiv(programId, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 1) {
                char *infoLog = static_cast<char *>(malloc(sizeof(char) * infoLen));
                glGetProgramInfoLog(programId, infoLen, NULL, infoLog);
                LOGE(getDrawerType(), "Error linking program \n%s\n", infoLog);
                free(infoLog);
            }
            glDeleteProgram(programId);
            return;
        }
        glClearColor(0, 0, 0, 0);
    }
}

void IDrawer::setViewSize(int width, int height) {
    this->width = width;
    this->height = height;
}

void IDrawer::setEGLImage(EGLImageKHR eglImageKhr) {
    this->eglImageKhr = eglImageKhr;
}

IDrawer::~IDrawer() {
};

IDrawer::IDrawer() {
}

void IDrawer::release() {

};
