package com.luffyxu.mulmedia.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luffy.mulmedia.R
import com.luffyxu.mulmedia.model.MediaInfoItem

class MediaInfoAdapter : RecyclerView.Adapter<MediaInfoAdapter.VH>() {

    var itemClickListener: (view: View, pos: Int) -> Unit? = { _, _ ->
    }

    var data: MutableList<MediaInfoItem> = mutableListOf()

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView
        val value: TextView

        init {
            title = itemView.findViewById(R.id.title)
            value = itemView.findViewById(R.id.value)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_media_info, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener(it, position)
        }
        holder.title.text = data[position].title
        holder.value.text = data[position].value
    }

    override fun getItemCount(): Int = data.size
}