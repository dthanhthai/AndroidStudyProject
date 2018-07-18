package com.example.doanthanhthai.mangafox.adapter

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.example.doanthanhthai.mangafox.R
import com.example.doanthanhthai.mangafox.model.Anime
import com.squareup.picasso.Picasso

import java.util.ArrayList

/**
 * Created by DOAN THANH THAI on 7/12/2018.
 */

class SlideBannerAdapter(private val animeList: List<Anime>, private val mListener: OnSlideBannerAdapterListener?) : PagerAdapter() {

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return animeList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val myLayout = LayoutInflater.from(view.context)
                .inflate(R.layout.item_slide_banner, view, false)
        val myImage = myLayout
                .findViewById<View>(R.id.image_banner) as ImageView

        val titleTv = myLayout.findViewById<TextView>(R.id.anime_title)
        val episodeTv = myLayout.findViewById<TextView>(R.id.episode_info)
        val rateTv = myLayout.findViewById<TextView>(R.id.anime_rate)

        val animeData = animeList[position]

        Picasso.with(view.context)
                .load(animeData.bannerImage)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(myImage)

        titleTv.text = animeData.title
        episodeTv.text = animeData.episodeInfo
        rateTv.text = animeData.rate

        myLayout.setOnClickListener {
            mListener?.onBannerClick(animeList[position], position)
        }

        view.addView(myLayout, 0)
        return myLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    interface OnSlideBannerAdapterListener {
        fun onBannerClick(item: Anime, position: Int)
    }
}
