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
import com.luffyxu.camera.CameraNativeRender
import com.luffyxu.camera.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraNativeFragment(val cameraId: Int = 0) : Fragment() {

    lateinit var viewBinding: FragmentCameraBinding

    lateinit var cameraClient: CameraClient

    var surfaceHolder: SurfaceHolder? = null

    var surface: Surface? = null

    val previewSize: Size? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentCameraBinding.inflate(inflater)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onCreate")
        viewBinding.apply {
            Log.d(TAG, "surface addCallback")
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

        val render = CameraNativeRender(cameraClient)
        render.setSurfaceView(viewBinding.surfacePreview)

        cameraClient.previewFrameCallback = { image, data, width, height ->
            render.updateImageBuffer(image.hardwareBuffer)
            image.close()
        }
    }

    private fun galleryAddPic(image: File) {
        val insertUri: Uri =
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )!!
        val bitmap = BitmapFactory.decodeFile(image.path)
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            requireContext().contentResolver.openOutputStream(insertUri)
        )
        requireContext().sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(image)
            )
        )
    }

    companion object {
        const val TAG = "CameraNativeFragment"
    }

    override fun onDestroyView() {
        lifecycleScope.launch {
            cameraClient?.closeCamera()
        }
        super.onDestroyView()
    }
}