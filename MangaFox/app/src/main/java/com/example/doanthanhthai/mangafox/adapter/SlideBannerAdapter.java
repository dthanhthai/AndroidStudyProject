package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/12/2018.
 */

public class SlideBannerAdapter extends PagerAdapter {
    private List<Anime> images;
    private OnSlideBannerAdapterListener mListener;

    public SlideBannerAdapter(ArrayList<Anime> images,OnSlideBannerAdapterListener listener) {
        this.images=images;
        mListener = listener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View myImageLayout = LayoutInflater.from(view.getContext())
                .inflate(R.layout.item_slide_banner, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image_banner);

        Picasso.with(view.getContext())
                .load(images.get(position).bannerImage)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(myImage);

        myImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onBannerClick(images.get(position), position);
                }
            }
        });

        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public interface OnSlideBannerAdapterListener {
        void onBannerClick(Anime item, int position);
    }
}
