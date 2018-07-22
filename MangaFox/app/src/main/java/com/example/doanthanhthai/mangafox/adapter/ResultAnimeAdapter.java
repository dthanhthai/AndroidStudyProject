package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class ResultAnimeAdapter extends RecyclerView.Adapter<ResultAnimeAdapter.ResultAnimeViewHolder> {
    private List<Anime> episodeList;
    private OnResultAnimeAdapterListener mListener;

    public ResultAnimeAdapter(OnResultAnimeAdapterListener listener) {
        this.episodeList = new ArrayList<>();
        mListener = listener;
    }

    public void setEpisodeList(List<Anime> episodeList) {
        this.episodeList = episodeList;
        notifyDataSetChanged();
    }

    @Override
    public ResultAnimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new ResultAnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultAnimeViewHolder holder, final int position) {
        final Anime item = episodeList.get(position);
        holder.bindView(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(item, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public class ResultAnimeViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImg;
        TextView animeTitleTv;
        TextView animeEpisodeInfoTv;
        Context mContext;

        public ResultAnimeViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            posterImg = itemView.findViewById(R.id.anime_poster);
            animeTitleTv = itemView.findViewById(R.id.anime_title);
            animeEpisodeInfoTv = itemView.findViewById(R.id.episode_info);
        }

        public void bindView(Anime anime) {
            if (!TextUtils.isEmpty(anime.image)) {

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.placeholder);

                Glide.with(mContext)
                        .load(anime.image)
                        .thumbnail(0.4f)
                        .apply(requestOptions)
                        .into(posterImg);

//                Picasso.with(mContext)
//                        .load(anime.image)
//                        .error(R.drawable.placeholder)
//                        .placeholder(R.drawable.placeholder)
//                        .into(posterImg);
            }

            animeTitleTv.setText(anime.title);
            animeEpisodeInfoTv.setText(anime.episodeInfo);
            Log.i("Anime result name: ", anime.title);
        }
    }

    public interface OnResultAnimeAdapterListener {
        void onItemClick(Anime item, int position);
    }
}
