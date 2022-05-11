package com.xzq.nativelib2

class NativeLib {

    /**
     * A native method that is implemented by the 'nativelib2' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'nativelib2' library on application startup.
        init {
            System.loadLibrary("nativelib2")
        }
    }
}