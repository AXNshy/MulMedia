package com.luffyxu.camera

import android.util.Size

class CameraSize(val size: Size) {
    val long = if(size.width >size.height) size.width else size.height
    val short = if(size.width >size.height) size.height else size.width
}