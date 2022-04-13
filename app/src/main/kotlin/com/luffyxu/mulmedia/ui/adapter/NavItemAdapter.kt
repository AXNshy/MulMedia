package com.luffyxu.mulmedia.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luffy.mulmedia.R
import com.luffyxu.mulmedia.model.NavItem

class NavItemAdapter : RecyclerView.Adapter<NavItemAdapter.VH>() {

    var itemClickListener : (view : View,pos : Int) -> Unit? =  {
            _, _ ->
    }

    var data : MutableList<NavItem> = mutableListOf()
    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView
        init {
            title = itemView.findViewById(R.id.nav_item_title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_text,parent,false)).apply {  }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener(it,position)
        }
        holder.title.text = data[position].title
    }

    override fun getItemCount(): Int = data.size
}