package com.luffyxu.mulmedia.gles3.egl

import android.opengl.*
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.luffy.mulmedia.egl.RenderState
import com.luffy.mulmedia.egl.RenderThread
import com.luffy.mulmedia.gles2.IDrawer
import com.luffy.mulmedia.utils.OpenGLUtils

class RenderThread : Thread("GL3-RenderThread") {
    val TAG = "GL3-RenderThread"

    val drawers : MutableList<IDrawer> = mutableListOf()

    var surface : Surface? = null

    @Volatile
    var render : Boolean = true


    private var isEGLCreated = false
    private var isBindEGLSurface = false

    private var mCurrentState = RenderState.NO_SURFACE


    private var width = 0
    private var height = 0

    fun addDrawer(dr : IDrawer){
        drawers.add(dr)
    }

    override fun run() {
        super.run()
        render = true
        if(!init()){
            Log.d(TAG,"EGL init fail")
            return
        }
        try {
            while (render){

                when (mCurrentState) {
                    RenderState.RENDERING -> draw()
                    RenderState.SURFACE_DESTROY -> {
                        destroyEGLSurface()
                        mCurrentState = RenderState.NO_SURFACE
                    }
                    RenderState.FRESH_SURFACE -> {
                        createEGLSurfaceFirst()
                        holdOn()
                    }
                    RenderState.SURFACE_CHANGE -> {
                        createEGLSurfaceFirst()
                        configWorldSize()
                        mCurrentState = RenderState.RENDERING
                    }
                    RenderState.STOP -> {
                        releaseEGL()
                        return
                    }
                    else -> holdOn()
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
    lateinit var eglContext:EGLContext
    lateinit var eglConfig:EGLConfig

    fun initEGL() : Boolean{
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
        eglConfig = config[0]!!
        return true;
    }

    private fun init() : Boolean{
        return initEGL()
    }

    private fun swapBuffer (display: EGLDisplay,surface:EGLSurface){
        EGL14.eglSwapBuffers(display,surface)
    }

    private fun release(){
        Log.d(TAG, "release")
        mCurrentState = RenderState.NO_SURFACE
        notifyGo()
    }


    private val waitLock = Object()

    fun notifyGo() {
        Log.d(RenderThread.TAG, "notifyGo")
        synchronized(waitLock) { waitLock.notify() }
    }

    fun holdOn() {
        Log.d(RenderThread.TAG, "holdOn")
        synchronized(waitLock) {
            try {
                waitLock.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }


    fun onSurfaceCreated(holder: SurfaceHolder) {
        Log.d(RenderThread.TAG, "onSurfaceCreated")
//        mSurfaceHolder = WeakReference(holder)
        mCurrentState = RenderState.FRESH_SURFACE
        notifyGo()
    }


    fun onSurfaceChanged(width: Int, height: Int) {
        Log.d(RenderThread.TAG, "onSurfaceChanged")
        this.width = width
        this.height = height
        mCurrentState = RenderState.SURFACE_CHANGE
        notifyGo()
    }


    fun onSurfaceDestroy() {
        Log.d(RenderThread.TAG, "onSurfaceDestroy")
        mCurrentState = RenderState.SURFACE_DESTROY
        notifyGo()
    }

    private fun draw() {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        for (drawer in drawers) {
            drawer.draw()
            swapBuffer(egldisplay,eglSurface)
        }
    }




    private fun createEGLSurfaceFirst() {
        Log.d(RenderThread.TAG, "createEGLSurfaceFirst")
        GLES30.glViewport(0, 0, width, height)
        if (!isEGLCreated) {
            isEGLCreated = true
            createEGLSurface()
            if (!isBindEGLSurface) {
                isBindEGLSurface = true
//                generateTextureId()
            }
        }
    }

    private fun generateTextureId() {
        Log.d(RenderThread.TAG, "generateTextureId")
        val textureIds = OpenGLUtils.createTextureId(drawers.size)
        var i = 0
        for (drawer in drawers) {
//            drawer.setTextureId(textureIds[i])
            i++
        }
    }

    private fun createEGLSurface() {
        Log.d(RenderThread.TAG, "createEGLSurface")
//        if (mSurfaceHolder != null) {
//            mEGLHolder.createEGLSurface(mSurfaceHolder.get().getSurface(), -1, -1)
//            mEGLHolder.makeCurrent()
//        }
        if(surface == null) {
            Log.d(TAG, "surface is null")
            return
        }
        eglSurface = EGL14.eglCreateWindowSurface(egldisplay,eglConfig,surface,
                intArrayOf(EGL14.EGL_NONE),0)
        if(eglSurface == EGL14.EGL_NO_SURFACE){
            Log.d(TAG, "eglCreateWindowSurface fail $eglSurface")
            return;
        }
        eglContext = EGL14.eglCreateContext(egldisplay,eglConfig,
                EGL14.EGL_NO_CONTEXT,contextAttribe,0)
        if(eglContext == EGL14.EGL_NO_CONTEXT){
            Log.d(TAG, "eglCreateContext fail $eglContext")
            return;
        }

        if(!EGL14.eglMakeCurrent(egldisplay,eglSurface,eglSurface,eglContext)){
            Log.d(TAG, "eglMakeCurrent fail $egldisplay")
            return;
        }
    }

    private fun configWorldSize() {
        Log.d(RenderThread.TAG, "configWorldSize")
        for (drawer in drawers) {
            drawer.setSurfaceSize(width, height)
        }
    }

    fun destroyEGLSurface() {
        Log.d(RenderThread.TAG, "destroyEGLSurface")
        EGL14.eglMakeCurrent(egldisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        isBindEGLSurface = false
    }

    fun releaseEGL() {
        Log.d(RenderThread.TAG, "releaseEGL")
        if (egldisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(egldisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(egldisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(egldisplay)
        }

        egldisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
//        mEGLConfig = null
    }
}