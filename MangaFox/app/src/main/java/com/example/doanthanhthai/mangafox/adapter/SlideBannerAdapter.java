package com.example.doanthanhthai.mangafox.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/12/2018.
 */

public class SlideBannerAdapter extends PagerAdapter {
    private List<Anime> animeList;
    private OnSlideBannerAdapterListener mListener;

    public SlideBannerAdapter(List<Anime> animeList, OnSlideBannerAdapterListener listener) {
        this.animeList = animeList;
        mListener = listener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return animeList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View myLayout = LayoutInflater.from(view.getContext())
                .inflate(R.layout.item_slide_banner, view, false);
        ImageView myImage = (ImageView) myLayout
                .findViewById(R.id.image_banner);

        TextView titleTv = myLayout.findViewById(R.id.anime_title);
        TextView episodeTv = myLayout.findViewById(R.id.episode_info);
        TextView rateTv = myLayout.findViewById(R.id.anime_rate);

        Anime animeData = animeList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.nature_cover);
        requestOptions.error(R.drawable.nature_cover);

        Glide.with(view.getContext())
                .load(animeData.getBannerImage())
                .thumbnail(0.2f)
                .apply(requestOptions)
                .into(myImage);

//        Picasso.with(view.getContext())
//                .load(animeData.bannerImage)
//                .error(R.drawable.placeholder)
//                .placeholder(R.drawable.placeholder)
//                .into(myImage);

        titleTv.setText(animeData.getTitle());
        episodeTv.setText(animeData.getEpisodeInfo());
        rateTv.setText(animeData.getRate());

        myLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onBannerClick(animeList.get(position), position, view);
                }
            }
        });

        view.addView(myLayout, 0);
        return myLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public interface OnSlideBannerAdapterListener {
        void onBannerClick(Anime item, int position, View view);
    }
}
