package com.luffyxu.camera.ui.fragment

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.luffyxu.camera.R
import com.luffyxu.camera.databinding.FragmentNavigateBinding

class CameraNavigationFragment : Fragment() {

    companion object {
        const val TAG = "CameraNavigationFragment"
    }

    lateinit var binding: FragmentNavigateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigateBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        binding.rvUseCases.apply {
            val cameraManager =
                requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val ids = cameraManager.cameraIdList
            Log.d(TAG, "ids ${ids.toList()}")
            findNavController().navigate(R.id.action_cameraNavigationFragment_to_cameraNativeFragment)

//            adapter =
//                GenericListAdapter(ids.toList(), R.layout.item_usecase, onBind = { v, i, p ->
//                    Log.d(TAG, "onBind position($p), data:${i}")
//                    val text = v.findViewById<TextView>(R.id.nav_item_title) as TextView
//                    text.text = i.toString()
//                    v.setOnClickListener {
//                    }
//                })
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }


}