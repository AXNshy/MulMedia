package com.luffyxu.camera.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
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

    private val mHandler : Handler = object  : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    protected fun checkCameraPermissions(){

        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            mHandler.post(Runnable {
                onCameraAvailable(true)
            })

        }else{
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
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
                mHandler.post(Runnable {
                    onCameraAvailable(false)
                })
            }
            else ->{
                mHandler.post(Runnable {
                    onCameraUnavailable("permission fail")
                })
            }
        }
    }

    abstract fun onCameraAvailable(immiadate :Boolean)
    abstract fun onCameraUnavailable(msg :String)
}