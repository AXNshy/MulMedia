package com.luffyxu.mulmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.luffy.mulmedia.databinding.ActivityCameraBinding
import com.luffyxu.mulmedia.activity.fragment.Camera2Fragment
import com.luffyxu.mulmedia.activity.fragment.CameraFragment

class CameraActivity :CameraBaseActivity() {

    lateinit var viewBinding :ActivityCameraBinding

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
                return 1
            }

            override fun createFragment(position: Int): Fragment {
                return when(position){
                    1 -> CameraFragment()
                    else -> CameraFragment()
                }
            }

        }
    }

    override fun onCameraUnavailable(msg: String) {
        TODO("Not yet implemented")
    }
}