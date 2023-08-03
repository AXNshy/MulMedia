package com.luffy.mulmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luffy.mulmedia.R
import com.luffy.mulmedia.consts.Consts.titleArray
import com.luffy.mulmedia.databinding.ActivityNavigationBinding
import com.luffyxu.mulmedia.model.NavItem
import com.luffyxu.mulmedia.ui.adapter.NavItemAdapter
import java.io.FileDescriptor

class NavigationActivity : BaseActivity() {
    var mLessonPaths: MutableList<NavItem> = mutableListOf()
    lateinit var adapter: NavItemAdapter
    private var selectUri: Uri? = null
    lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        mLessonPaths = createNavList()
        adapter = NavItemAdapter().apply {
            data.addAll(mLessonPaths)
            itemClickListener = { view: View?, integer: Int ->
                if (integer == 0) {
                    if (checkPermission()) {
                        startFileBrowser()
                    } else {
                        requestPermission()
                    }
                } else {
                    navigate(integer)
                }
            }
        }
        binding.LessonListView.apply {
            adapter = this@NavigationActivity.adapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    itemPosition: Int,
                    parent: RecyclerView
                ) {
                    super.getItemOffsets(outRect, itemPosition, parent)
                    outRect.left = 20
                    outRect.right = 20
                    outRect.bottom = 20
                }
            })
            layoutManager =
                LinearLayoutManager(this@NavigationActivity, RecyclerView.VERTICAL, false)
        }
    }

    private fun checkPermission(): Boolean {
        val code =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return code == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "权限申请成功")
            } else {
                Log.d(TAG, "权限申请失败")
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun createNavList(): MutableList<NavItem> {
        val pathArrays = resources.getStringArray(R.array.media_lesson_path_collection)
        val titleArrays = titleArray()
        val data: MutableList<NavItem> = ArrayList()
        for (i in titleArrays.indices) {
            if (i == 0) {
                data.add(NavItem(titleArrays[i], ""))
            } else {
                data.add(NavItem(titleArrays[i], pathArrays[i - 1]))
            }
        }
        return data
    }

    private fun navigate(position: Int) {
        val path = mLessonPaths[position].path
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("xzq://navigate$path"))
        intent.putExtra("uri", selectUri)
        startActivity(intent)
    }

    private fun startFileBrowser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "video/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val uri = data!!.data
            Log.d(TAG, "onActivityResult $uri")
            selectUri = uri
            adapter.data[0].description = uri.toString()
            adapter.notifyItemChanged(0)
        }
    }

    override fun onUriAction(uri: Uri?) {
    }

    override fun onUriAction(uri: FileDescriptor?) {
    }

    companion object {
        private const val TAG = "NavigationActivity"
    }
}