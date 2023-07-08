package com.luffyxu.mulmedia.activity

import android.graphics.Rect
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luffy.mulmedia.R
import com.luffy.mulmedia.databinding.ActivityMediaInfoBinding
import com.luffyxu.mulmedia.model.MediaInfoItem
import com.luffyxu.mulmedia.ui.adapter.MediaInfoAdapter
import com.luffyxu.opengles.base.egl.FileUtils

class MediaInfoActivity : AppCompatActivity(R.layout.activity_media_info) {
    lateinit var recyclerView : RecyclerView
    lateinit var binding : ActivityMediaInfoBinding
    var adapter : MediaInfoAdapter? = null
    var filepath : Uri? = null

    val mediaInfos = mutableListOf<MediaInfoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaInfoBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_media_info)
        recyclerView = binding.rvMediaInfo

        filepath = intent.getParcelableExtra<Uri>("uri")
        if (filepath != null) {
            val realPath = FileUtils.getPath(applicationContext, filepath!!)
            filepath = Uri.parse(realPath)
            initData()
        }
    }

    fun initData(){
        adapter = MediaInfoAdapter()
        recyclerView.addItemDecoration( object :RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                super.getItemOffsets(outRect, itemPosition, parent)
                outRect.left = 20
                outRect.right = 20
                outRect.bottom = 20
            }
        });
        recyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)

        startParseMediaInfo()
    }

    private fun startParseMediaInfo(){
        val extractor: MediaExtractor = MediaExtractor()
        try {
            extractor.setDataSource(filepath?.path?:"")

            val countOfTrack = extractor.trackCount
            for( i in 0 until  countOfTrack){
                var track = extractor.getTrackFormat(i)
                track.apply {
                    var mime = track.getString(MediaFormat.KEY_MIME)?:""
                    if(mime.startsWith("audio")){
                        mediaInfos.add(MediaInfoItem("","Audio$i"))
                    }else if(mime.startsWith("video")){
                        mediaInfos.add(MediaInfoItem("","Video$i"))
                    }else{
                        mediaInfos.add(MediaInfoItem("","Other$i"))
                    }
                    for(k in keys){

                        val v : String= when(getValueTypeForKey(k)) {
                            MediaFormat.TYPE_STRING -> getString(k) ?: ""
                            MediaFormat.TYPE_INTEGER -> getInteger(k,0).toString() ?: ""
                            MediaFormat.TYPE_LONG -> getLong(k,0).toString() ?: ""
                            MediaFormat.TYPE_FLOAT -> getFloat(k,0f).toString() ?: ""
                            else -> "unknown"
                        }
                        mediaInfos.add(MediaInfoItem(k,v))
                    }
                    mediaInfos.add(MediaInfoItem("",""))
                }
            }

            adapter?.data = mediaInfos
            recyclerView.adapter = adapter
            adapter?.notifyDataSetChanged()
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            extractor.release()
        }
    }
}