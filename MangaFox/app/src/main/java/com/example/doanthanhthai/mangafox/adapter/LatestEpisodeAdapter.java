package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.share.DynamicColumnHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class LatestEpisodeAdapter extends RecyclerView.Adapter<LatestEpisodeAdapter.LatestViewHolder> {
    private List<Anime> animeList;
    private OnLatestEpisodeAdapterListener mListener;
    private int widthItem = -1;
    private int spacing = -1;

    public LatestEpisodeAdapter(OnLatestEpisodeAdapterListener listener) {
        this.animeList = new ArrayList<>();
        mListener = listener;
    }

    public void setAnimeList(List<Anime> animeList) {
        this.animeList = animeList;
        notifyDataSetChanged();
    }

    public void setDynamicColumnHelper(DynamicColumnHelper helper) {
        widthItem = helper.getWidthItem();
        spacing = helper.getSpacing();
    }

    public void addMoreAnime(List<Anime> animeList) {
        int indexBegin = this.animeList.size();
        for (Anime anime : animeList) {
            this.animeList.add(anime);
        }
        notifyItemRangeChanged(indexBegin, animeList.size());
//        notifyDataSetChanged();
    }

    @Override
    public LatestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lastest_episode, parent, false);
        return new LatestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LatestViewHolder holder, final int position) {
        final Anime item = animeList.get(position);
        holder.bindView(item, position);
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
        return animeList.size();
    }

    public class LatestViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImg;
        TextView animeTitleTv, episodeTitleTv, rateTv;
        Context mContext;

        public ImageView getPosterImg() {
            return posterImg;
        }

        public TextView getAnimeTitleTv() {
            return animeTitleTv;
        }

        public LatestViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            posterImg = itemView.findViewById(R.id.episode_poster);
            animeTitleTv = itemView.findViewById(R.id.anime_title);
            episodeTitleTv = itemView.findViewById(R.id.episode_title);
            rateTv = itemView.findViewById(R.id.anime_rate);
        }

        public void bindView(Anime anime, int position) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            layoutParams.width = widthItem;
            layoutParams.leftMargin = spacing;
            layoutParams.rightMargin = spacing;
            layoutParams.bottomMargin = spacing;
            layoutParams.topMargin = spacing;
            itemView.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams posterParams = (RelativeLayout.LayoutParams) posterImg.getLayoutParams();
            posterParams.height = (int) (widthItem * 3.5 / 3);
            posterImg.setLayoutParams(posterParams);

            if (!TextUtils.isEmpty(anime.getImage())) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.color.slight_gray);
                requestOptions.error(R.color.slight_gray);

                Glide.with(mContext)
                        .load(anime.getImage())
                        .thumbnail(0.4f)
                        .apply(requestOptions)
                        .into(posterImg);
            }

            animeTitleTv.setText(anime.getTitle());
            episodeTitleTv.setText(anime.getEpisodeInfo());
        }
    }

    public interface OnLatestEpisodeAdapterListener {
        void onItemClick(Anime item, int position);
    }
}
