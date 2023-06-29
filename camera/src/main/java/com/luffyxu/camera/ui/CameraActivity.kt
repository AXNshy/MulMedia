package com.luffyxu.camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.luffyxu.camera.databinding.ActivityCameraBinding
import com.luffyxu.camera.ui.fragment.CameraFragment
import com.luffyxu.camera.ui.fragment.CameraNativeFragment

class CameraActivity : CameraBaseActivity() {

    lateinit var viewBinding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
        checkCameraPermissions()
    }

    override fun onCameraAvailable(immiadate: Boolean) {
//        viewBinding.viewPager
        viewBinding.viewPager.adapter = object : FragmentStateAdapter(this){
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> CameraNativeFragment()
//                    2 -> Camera2Fragment()
//                    3 -> CameraNativeFragment()
                    else -> CameraFragment()
                }
            }

        }
    }

    override fun onCameraUnavailable(msg: String) {
        TODO("Not yet implemented")
    }
}