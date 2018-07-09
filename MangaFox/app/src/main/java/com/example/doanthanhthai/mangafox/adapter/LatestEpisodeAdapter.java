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

import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class LatestEpisodeAdapter extends RecyclerView.Adapter<LatestEpisodeAdapter.LatestViewHolder> {
    private List<Anime> episodeList;
    private OnLatestEpisodeAdapterListener mListener;

    public LatestEpisodeAdapter(OnLatestEpisodeAdapterListener listener) {
        this.episodeList = new ArrayList<>();
        mListener = listener;
    }

    public void setEpisodeList(List<Anime> episodeList) {
        this.episodeList = episodeList;
        notifyDataSetChanged();
    }

    @Override
    public LatestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lastest_episode, parent, false);
        return new LatestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LatestViewHolder holder, final int position) {
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

    public class LatestViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImg;
        TextView animeTitleTv, episodeTitleTv;
        Context mContext;

        public LatestViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            posterImg = itemView.findViewById(R.id.episode_poster);
            animeTitleTv = itemView.findViewById(R.id.anime_title);
            episodeTitleTv = itemView.findViewById(R.id.episode_title);
        }

        public void bindView(Anime anime) {
            if (!TextUtils.isEmpty(anime.image)) {

                Picasso.with(mContext)
                        .load(anime.image)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(posterImg);
            }

            animeTitleTv.setText(anime.title);
            Log.i("Episode name", anime.episode.name);
            if (anime.episode.name.contains("Tập")) {
                if (anime.episode.name.contains("-")) {
                    episodeTitleTv.setText(anime.episode.name.substring(0, anime.episode.name.indexOf("-")).trim());
                } else {
                    episodeTitleTv.setText(anime.episode.name.substring(0, 5).trim());
                }
            } else {
                episodeTitleTv.setText(anime.episode.name);
            }

        }
    }

    public interface OnLatestEpisodeAdapterListener {
        void onItemClick(Anime item, int position);
    }
}
