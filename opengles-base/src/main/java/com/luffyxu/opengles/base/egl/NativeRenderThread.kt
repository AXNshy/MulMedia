package com.luffyxu.opengles.base.egl

import android.util.Log
import android.view.Surface

class NativeRenderThread :Thread() {
    var native_renderer : Int = -1;
    companion object{
        val TAG = "NativeRenderThread"
        init {
            System.loadLibrary("native-render")
        }
    }
    init {

    }

    override fun run() {
        super.run()
        native_renderer = nativeCreateRenderer()
        nativeRun(native_renderer)
    }


    private external fun nativeCreateRenderer() : Int
    private external fun nativeRun(native_render:Int)
    external fun onSurfaceCreate(native_render:Int ,surface: Surface)
    external fun onSurfaceChanged(native_render:Int,surface: Surface,width:Int,height:Int)
    external fun onSurfaceDestroyed(native_render:Int)
}