package com.luffyxu.opengles.base.egl

import android.opengl.EGLContext
import android.opengl.EGLSurface

class EGLSurfaceHolder {
    private var mEGLCore: EGLCore? = null
    private var mEGLSurface: EGLSurface? = null
    fun init(context: EGLContext?, flags: Int, glVersion: Int) {
        mEGLCore = EGLCore()
        mEGLCore!!.init(context, flags, glVersion)
    }

    fun createEGLSurface(surface: Any?, width: Int, height: Int) {
        mEGLSurface = if (surface != null) {
            mEGLCore!!.createWindowSurface(surface)
        } else {
            mEGLCore!!.createOffScreenSurface(width, height)
        }
    }

    fun swapBuffer() {
        if (mEGLSurface != null) {
            mEGLCore!!.swapBuffers(mEGLSurface)
        }
    }

    fun makeCurrent() {
        if (mEGLSurface != null) {
            mEGLCore!!.makeCurrent(mEGLSurface)
        }
    }

    fun destroyEGLSurface() {
        if (mEGLCore != null) {
            mEGLCore!!.destroySurface(mEGLSurface)
            mEGLSurface = null
        }
    }

    fun release() {
        if (mEGLCore != null) {
            mEGLCore!!.release()
        }
    }

    companion object {
        const val TAG = "EGLSurfaceHolder"
    }
}