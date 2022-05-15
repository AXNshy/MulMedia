package com.xzq.nativelib

import android.util.Log
import android.view.Surface

class FFmpegPlayer(var path:String?) {

    companion object{
        val TAG = "FFmpegPlayer"
        init {
            System.loadLibrary("nativelib")
        }
    }

    var native_player : Int = -1;

    fun createPlayer(surface: Surface?){
        Log.d(TAG,"createPlayer path$path  surface:$surface")
        native_player = createPlayer(path!!,surface)
    }

    fun play(){
        play(native_player)
    }


    fun pause(){
        pause(native_player)
    }

    fun stop(){
        stop(native_player)
    }
    external fun ffmpegInfo() : String

    private external fun createPlayer(path:String, surface: Surface?) : Int

    private external fun play(player: Int)

    private external fun pause(player: Int)

    private external fun stop(player: Int)

}