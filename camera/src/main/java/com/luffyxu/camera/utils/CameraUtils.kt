package com.luffyxu.mulmedia.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.luffyxu.camera.SmartSize
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {

    const val TAG = "CameraUtils"
        fun computeExifOrientation(rotate : Int,mirrored : Boolean) : Int {
            return when {
                rotate > 315 && rotate < 45 && mirrored -> {
                    ExifInterface.ORIENTATION_NORMAL
                }
                rotate > 315 && rotate < 45 && !mirrored -> {
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL
                }
                rotate > 45 && rotate < 135 && mirrored -> {
                    ExifInterface.ORIENTATION_ROTATE_90
                }
                rotate > 45 && rotate < 135 && !mirrored -> {
                    ExifInterface.ORIENTATION_TRANSPOSE
                }
                rotate > 135 && rotate < 225 && mirrored -> {
                    ExifInterface.ORIENTATION_ROTATE_180
                }
                rotate > 135 && rotate < 225 && !mirrored -> {
                    ExifInterface.ORIENTATION_FLIP_VERTICAL
                }
                rotate > 225 && rotate < 315 && mirrored -> {
                    ExifInterface.ORIENTATION_ROTATE_270
                }
                rotate > 225 && rotate < 315 && !mirrored -> {
                    ExifInterface.ORIENTATION_TRANSVERSE
                }
                else -> ExifInterface.ORIENTATION_NORMAL
            }
        }

    fun createFile(context: Context, extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//           return   File(
//               "${context.getExternalFilesDir("Pictures")}/CameraDemo/",
//               "Image_${sdf.format(Date())}.$extension"
//           )
            return   File(
                "${context.cacheDir}/Pictures/CameraDemo/",
                "Image_${sdf.format(Date())}.$extension"
            )
        }else {
            return File(
                "${Environment.getExternalStorageDirectory()}/Pictures/CameraDemo/",
                "Image_${sdf.format(Date())}.$extension"
            )
        }
    }


    fun adjustSurfaceSize(camera: Size,screen:Size) :Size{
        Log.d(TAG,"adjustSurfaceSize camera:[$camera],screen:[$screen]")
        val rs = screen.width / screen.height
        val rc = camera.width/camera.height
        var width = -1
        var height = -1
        if(rc >= rs){
            width = screen.width
            height = screen.width * camera.height / camera.width
            return Size(width,height)
        }else{
            height = screen.height
            width = screen.height *camera.width / camera.height
            return Size(width, height)
        }
    }

    fun obtainScreenSize(context: Context) :Size{
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    fun checkPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun <T>getPreviewOutputSize(context: Context,characteristics:CameraCharacteristics,targetClass:Class<T>,format:Int? = null) : Size{
        val screenSize : Size = obtainScreenSize(context)
//        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
//        val maxSize = if (hdScreen) SIZE_1080P else screenSize
        val config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        val allSizes = config.getOutputSizes(targetClass)
        Log.d(TAG,"allSize:${allSizes}")
        return allSizes.first { it.height < screenSize.height && it.width < screenSize.width }
    }

    fun getSuitableSize(availableSizes: Array<Size>, display: Display): Size {
        Log.d(TAG,"availableSizes : ${availableSizes.joinToString { it.toString() }}")
        val point = Point()
        display.getRealSize(point)

        val screenSize = SmartSize(point.x,point.y)
        return availableSizes.sortedBy { it.width*it.height }.map { SmartSize(it.width,it.height) }.reversed().first { screenSize.long>it.long && screenSize.short > it.short }.size
    }
}