package com.luffyxu.mulmedia.gles3

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface
import com.luffy.mulmedia.gles2.IDrawer
import java.lang.Exception

class RenderThread : Thread("GL3-Render") {
    val TAG = "GL3-Render"

    val drawers : MutableList<IDrawer> = mutableListOf()

    var surface : Surface? = null

    fun addDrawer(dr : IDrawer){
        drawers.add(dr)
    }

    override fun run() {
        super.run()
        if(!init()){
            Log.d(TAG,"EGL init fail")
            return
        }
        try {
            while (true){
                for(draw in drawers){
                    draw.draw()
                    swapBuffer(egldisplay,eglSurface)
                }
                sleep(20)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        for(draw in drawers){
            draw.release()
        }

    }

    val versions: IntArray = IntArray(2)

    val configAttribe:IntArray = intArrayOf(
        EGL14.EGL_SURFACE_TYPE,
        EGL14.EGL_WINDOW_BIT,
        EGL14.EGL_RED_SIZE,8,
        EGL14.EGL_GREEN_SIZE,8,
        EGL14.EGL_BLUE_SIZE,8,
        EGL14.EGL_DEPTH_SIZE,24,
        EGL14.EGL_NONE
    )
    val contextAttribe:IntArray = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,3, EGL14.EGL_NONE)

    lateinit var egldisplay: EGLDisplay
    lateinit var eglSurface:EGLSurface

    fun initEGL(surface: Surface) : Boolean{
        egldisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if(egldisplay == EGL14.EGL_NO_DISPLAY){
            Log.d(TAG, "eglGetDisplay fail $egldisplay")
            return false
        }

        if(!EGL14.eglInitialize( egldisplay,versions,0,versions,1)){
            Log.d(TAG, "eglInitialize fail $egldisplay")
            return false
        }
        val config : Array<EGLConfig?> = arrayOfNulls(1)
        var configNum = intArrayOf(EGL14.EGL_NONE)
        if(!EGL14.eglChooseConfig(egldisplay,configAttribe,0,
                config,0,1, configNum,0)) {
            Log.d(TAG, "eglChooseConfig fail $config")
            return false;
        }
        if(config[0] == null){
            Log.d(TAG, "config choose fail $config")
            return false;
        }
        eglSurface = EGL14.eglCreateWindowSurface(egldisplay,config[0],surface,
            intArrayOf(EGL14.EGL_NONE),0)
        if(eglSurface == EGL14.EGL_NO_SURFACE){
            Log.d(TAG, "eglCreateWindowSurface fail $eglSurface")
            return false;
        }
        val eglContext = EGL14.eglCreateContext(egldisplay,config[0],
            EGL14.EGL_NO_CONTEXT,contextAttribe,0)
        if(eglContext == EGL14.EGL_NO_CONTEXT){
            Log.d(TAG, "eglCreateContext fail $eglContext")
            return false;
        }

        if(!EGL14.eglMakeCurrent(egldisplay,eglSurface,eglSurface,eglContext)){
            Log.d(TAG, "eglMakeCurrent fail $egldisplay")
            return false;
        }
        return true;
    }

    private fun init() : Boolean{
        if(surface == null) {
            Log.d(TAG, "surface is null")
            return false
        }
        return initEGL(surface!!)
    }

    private fun swapBuffer (display: EGLDisplay,surface:EGLSurface){
        EGL14.eglSwapBuffers(display,surface)
    }
}