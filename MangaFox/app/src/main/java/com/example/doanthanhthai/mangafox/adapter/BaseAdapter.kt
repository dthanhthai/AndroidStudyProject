package com.example.doanthanhthai.mangafox.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.example.doanthanhthai.mangafox.model.Anime

/**
 * Created by ThaiDT1 on 7/27/2018.
 */
class BaseAdapter<T>(var itemList: MutableList<T>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var widthItem: Int = -1
    var spacing: Int = -1

    public fun setListItem(itemList:ArrayList<T>) {
        this.itemList = itemList

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}