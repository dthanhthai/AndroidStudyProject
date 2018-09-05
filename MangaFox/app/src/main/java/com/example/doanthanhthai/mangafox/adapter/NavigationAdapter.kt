package com.example.doanthanhthai.mangafox.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.doanthanhthai.mangafox.R
import com.example.doanthanhthai.mangafox.model.NavigationModel
import kotlinx.android.synthetic.main.item_navigation.view.*;

/**
 * Created by ThaiDT1 on 8/16/2018.
 */
class NavigationAdapter(listItem: MutableList<NavigationModel>, listener: NavigationAdapterListener?) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {
    private var listItem: MutableList<NavigationModel>? = mutableListOf()
    private var mListener: NavigationAdapterListener? = null

    init {
        this.listItem?.addAll(listItem)
        mListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (listItem != null && listItem?.isNotEmpty()!!) {
            return listItem?.size!!
        } else {
            return 0
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: NavigationModel = listItem?.get(position)!!
        holder.bindView(item)
        holder.itemView.setOnClickListener {
            mListener?.onItemClicked(item)
        }
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var iconIv: ImageView = itemView?.ivNavigation!!
        var nameTv: TextView = itemView?.tvNavigationName!!

        fun bindView(data: NavigationModel) {
            nameTv.text = data.name
            if (data.isShowIcon) {
                iconIv.setImageResource(data.iconRes)
            } else {
                iconIv.visibility = View.INVISIBLE
            }
        }
    }

    interface NavigationAdapterListener {
        fun onItemClicked(item: NavigationModel)
    }
}