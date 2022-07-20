package com.luffyxu.mulmedia.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.ExifInterface
import androidx.core.app.ActivityCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {
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
        return File(context.filesDir, "CameraDemoImage_${sdf.format(Date())}.$extension")
    }


    fun checkPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}