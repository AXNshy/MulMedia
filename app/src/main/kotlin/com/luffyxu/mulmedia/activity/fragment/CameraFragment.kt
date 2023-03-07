package com.luffyxu.mulmedia.activity.fragment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.luffy.mulmedia.databinding.FragmentCameraBinding
import com.luffyxu.camera.CameraClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraFragment(val state : Int = 0) : Fragment() {

    lateinit var viewBinding : FragmentCameraBinding

    lateinit var cameraClient : CameraClient

    var surfaceHolder : SurfaceHolder? = null

    var surface : Surface? = null

    val cameraManager by lazy { requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding =  FragmentCameraBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onCreate")
        viewBinding.apply {
            Log.d(TAG,"surface addCallback")
            surfacePreview.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d(TAG,"surfaceCreated")
                    surfaceHolder = holder
//                    val previewSize = getPreviewSize(requireActivity().display!!,characteristics,SurfaceView::class.java)
//
//                        val size = cameraClient.getSuitableSurfaceSize(requireActivity().windowManager.defaultDisplay)
                    lifecycleScope.launch {
                        val inited = cameraClient.init(context!!,viewBinding.surfacePreview)
                        if(inited) {
                            cameraClient.openPreview(holder.surface)
                        }
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    Log.d(TAG,"surfaceChanged,width:$width,height:$height")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    Log.d(TAG,"surfaceDestroyed")
                    surfaceHolder = null
                }
            })

            btnCapture.setOnClickListener { _->
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = cameraClient.takePhoto()
                    cameraClient.savePhoto(result).apply {
                        galleryAddPic(this)
                    }
                }
            }
        }

        lifecycleScope.launch{
            cameraClient = CameraClient().apply {
                clientCallback = object : CameraClient.Companion.Callback {
                    override fun onCameraSizeChange(size: Size) {
                        Log.d(TAG,"onCameraSizeChange $size")
                        surfaceHolder?.setFixedSize(size.width,size.height)
                        lifecycleScope.launch {
//                                    cameraClient.closePreview()
//                                    cameraClient.openPreview()
                        }
                    }
                }
            }
        }
    }

    private fun galleryAddPic(image : File) {
//        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//        val contentUri: Uri = Uri.fromFile(image)
//        mediaScanIntent.setData(contentUri)
//        this.sendBroadcast(mediaScanIntent)
        val insertUri: Uri =
            context!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())!!
        val bitmap = BitmapFactory.decodeFile(image.path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, context!!.contentResolver.openOutputStream(insertUri))
        context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)))
    }


    companion object{
        const val TAG = "CameraBase"
    }
}