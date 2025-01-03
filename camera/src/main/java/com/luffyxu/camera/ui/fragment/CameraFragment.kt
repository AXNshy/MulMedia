package com.luffyxu.camera.ui.fragment

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.luffyxu.camera.CameraClient
import com.luffyxu.camera.databinding.FragmentCameraBinding
import com.luffyxu.camera.drawers.TextureDrawer
import com.luffyxu.camera.drawers.TextureShader
import com.luffyxu.camera.gles3.CameraGLES3Renderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraFragment(val cameraId: Int = 0) : Fragment() {

    lateinit var viewBinding: FragmentCameraBinding

    lateinit var cameraClient: CameraClient

    var surfaceHolder: SurfaceHolder? = null

    var surface: Surface? = null

    val previewSize: Size? = null

    val textureDrawer: TextureDrawer = TextureDrawer()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(inflater)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onCreate")
        viewBinding.apply {
            Log.d(TAG, "surface addCallback")
//            surfacePreview.holder.addCallback(object : SurfaceHolder.Callback {
//                override fun surfaceCreated(holder: SurfaceHolder) {
//                    Log.d(TAG, "surfaceCreated")
////                    val previewSize = getPreviewSize(
////                        viewBinding.surfacePreview.display,
////                        characteristics,
////                        SurfaceHolder::class.java
////                    )
//                    Log.d(
//                        TAG,
//                        "View finder size: ${viewBinding.surfacePreview.width} x ${viewBinding.surfacePreview.height}"
//                    )
//                    Log.d(TAG, "Selected preview size: $previewSize")
////                    viewBinding.surfacePreview.setAspectRatio(
////                        previewSize.width,
////                        previewSize.height
////                    )
//
//                    lifecycleScope.launch {
//                        cameraClient.startCameraWithEffect(viewBinding.surfacePreview)
//                    }
//                }
//
//                override fun surfaceChanged(
//                    holder: SurfaceHolder,
//                    format: Int,
//                    width: Int,
//                    height: Int
//                ) {
//                    Log.d(TAG, "surfaceChanged,width:$width,height:$height")
//                }
//
//                override fun surfaceDestroyed(holder: SurfaceHolder) {
//                    Log.d(TAG, "surfaceDestroyed")
//                    surfaceHolder = null
//                }
//            })

            btnCapture.setOnClickListener { _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = cameraClient.takePhoto()
                    cameraClient.savePhoto(result).apply {
                        galleryAddPic(this)
                    }
                }
            }
        }
        cameraClient = CameraClient(requireContext(), cameraId)

        textureDrawer.setupCameraClient(cameraClient)
        textureDrawer.setVideoSize(1920, 1080)
        textureDrawer.setShader(TextureShader(requireContext()))
        val render = CameraGLES3Renderer(listOf(textureDrawer), 3, cameraClient)
        render.setSurfaceView(viewBinding.surfacePreview)

    }

    private fun galleryAddPic(image: File) {
//        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//        val contentUri: Uri = Uri.fromFile(image)
//        mediaScanIntent.setData(contentUri)
//        this.sendBroadcast(mediaScanIntent)
        val insertUri: Uri =
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )!!
        val bitmap = BitmapFactory.decodeFile(image.path)
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            requireContext().contentResolver.openOutputStream(insertUri)!!
        )
        requireContext().sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(image)
            )
        )
    }

    companion object {
        const val TAG = "CameraBase"
    }

    override fun onDestroyView() {
        lifecycleScope.launch {
            cameraClient.closeCamera()
        }
        super.onDestroyView()
    }
}