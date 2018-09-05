package com.example.doanthanhthai.mangafox.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.doanthanhthai.mangafox.R
import com.example.doanthanhthai.mangafox.model.NavigationModel
import com.example.doanthanhthai.mangafox.model.RelatedContent

/**
 * Created by ThaiDT1 on 9/4/2018.
 */
class RelatedContentAdapter(listItem: MutableList<RelatedContent>, listener: RelatedAdapterListener) : RecyclerView.Adapter<RelatedContentAdapter.RelatedContentViewHolder>() {

    private var listItem: MutableList<RelatedContent>? = mutableListOf()
    private var mListener: RelatedAdapterListener? = null

    init {
        this.listItem = listItem
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedContentViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_related_content, parent, false)
        return RelatedContentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (listItem != null && listItem?.isNotEmpty()!!) {
            listItem?.size!!
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: RelatedContentViewHolder, position: Int) {
        listItem?.let {
            var data: RelatedContent = it.get(position)!!
            holder.bindView(data)
            holder.itemView.setOnClickListener {
                mListener?.let {
                    it.onItemClicked(data)
                }
            }
        }
    }

    inner class RelatedContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var numnerEpisodeTv: TextView
        internal var wrapperLayout: FrameLayout
        internal var mContext: Context = itemView.context

        init {
            numnerEpisodeTv = itemView.findViewById(R.id.number_episode_tv)
            wrapperLayout = itemView.findViewById(R.id.number_episode_layout)

        }

        fun bindView(data: RelatedContent) {
            numnerEpisodeTv.text = data.name
            if (data.isCurrent) {
                wrapperLayout.setBackgroundColor(mContext.resources.getColor(R.color.cyan))
                itemView.isEnabled = false
//            } else if (!TextUtils.isEmpty(data.directUrl)) {
//                wrapperLayout.setBackgroundColor(mContext.resources.getColor(R.color.numEpViewedColor))
//                itemView.isEnabled = true
            } else {
                wrapperLayout.setBackgroundColor(mContext.resources.getColor(R.color.light_gray))
                itemView.isEnabled = true
            }
            Log.i("Episode data: ", data.name!! + "")
        }
    }

    interface RelatedAdapterListener {
        fun onItemClicked(item: RelatedContent)
    }
}