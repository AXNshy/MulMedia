package com.luffyxu.camera.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.luffyxu.camera.R

class PermissionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkCameraPermissions()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected fun checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                permissions[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PermissionFragment", "checkCameraPermissions true")
            findNavController().navigate(R.id.action_permissionFragment_to_cameraNavigationFragment)
        } else {
            Log.d("PermissionFragment", "checkCameraPermissions false")
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("PermissionFragment", "onRequestPermissionsResult $requestCode")
        when (requestCode) {
            REQUEST_CODE -> {
                for (index in permissions.indices) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        Log.d("PermissionFragment", "permission [${permissions[index]}] deny")
                        requireActivity().finish()
                    }
                }
                findNavController().navigate(R.id.action_permissionFragment_to_cameraNavigationFragment)
            }

            else -> {
                requireActivity().finish()
            }
        }
    }

    companion object {
        val permissions: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        const val REQUEST_CODE: Int = 1
    }
}