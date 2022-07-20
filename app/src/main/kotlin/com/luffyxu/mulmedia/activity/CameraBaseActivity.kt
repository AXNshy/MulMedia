package com.luffyxu.mulmedia.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

abstract class CameraBaseActivity : AppCompatActivity() {


    companion object{
        val permissions : Array<String> = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        const val REQUEST_CODE:Int = 1
        const val TAG :String = "CameraBaseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraPermissions()
    }


    private fun checkCameraPermissions(){

        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            cameraAvailable(true)
        }else{
            ActivityCompat.requestPermissions(this, permissions,REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE ->{
                for(index in permissions.indices){
                    if(grantResults[index] != PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG,"permission [${permissions[index]}] deny")
                        return
                    }
                }
                cameraAvailable(false)
            }
            else ->{

            }
        }
    }

    abstract fun cameraAvailable(immiadate :Boolean)
}