package com.luffyxu.camera.ui.fragment

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.luffyxu.camera.R
import com.luffyxu.camera.databinding.FragmentNavigateBinding
import com.luffyxu.camera.ui.adapter.GenericListAdapter
import kotlinx.coroutines.launch

class CameraNavigationFragment : Fragment() {

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
        binding.rvUseCases.apply {
            lifecycleScope.launch {
                val cameraManager =
                    requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val ids = cameraManager.cameraIdList
                adapter =
                    GenericListAdapter(ids.toList(), R.layout.item_usecase, onBind = { v, i, p ->
                        Log.d("GenericListAdapter", "onBind position($p), data:${i}")
                        val text = v.findViewById<TextView>(R.id.nav_item_title) as TextView
                        text.text = i.toString()
                    })
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
        }
    }
}