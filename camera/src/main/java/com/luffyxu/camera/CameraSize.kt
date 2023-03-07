package com.luffyxu.camera

import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.Display
import kotlin.math.max
import kotlin.math.min

class SmartSize(witdh : Int,height:Int){
    var short :Int = min(witdh,height)
    var long :Int = max(witdh,height)
    val size : Size = Size(witdh,height)
    override fun toString(): String = "SmartSize(${long}x$short)"
}

val SMARTSIZE_1080P = SmartSize(1080,1920)

fun <T>getPreviewSize(display:Display,characteristics: CameraCharacteristics,targetClass:Class<T>,format:Int?=null) : Size{
    val screenSize = Point().run {
        display.getRealSize(this)
        SmartSize(x,y)
    }

    val maxSize = if(screenSize.long >= SMARTSIZE_1080P.long || screenSize.short >= SMARTSIZE_1080P.short){
        SMARTSIZE_1080P
    }else{
        screenSize
    }

    val config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
    val allSize = if(format == null){
        config.getOutputSizes(targetClass)
    }else {
        config.getOutputSizes(format)
    }

    val validSize = allSize.sortedWith(
        compareBy { it.width * it.height }
    ).map { SmartSize(it.width,it.height) }.reversed()

    return validSize.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
}