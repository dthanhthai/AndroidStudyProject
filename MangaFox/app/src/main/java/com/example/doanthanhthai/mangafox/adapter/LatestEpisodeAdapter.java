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
import com.example.doanthanhthai.mangafox.model.Episode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class LatestEpisodeAdapter extends RecyclerView.Adapter<LatestEpisodeAdapter.LatestViewHolder> {
    private List<Episode> episodeList;
    private OnLatestEpisodeAdapterListener mListener;

    public LatestEpisodeAdapter(OnLatestEpisodeAdapterListener listener) {
        this.episodeList = new ArrayList<>();
        mListener = listener;
    }

    public void setEpisodeList(List<Episode> episodeList) {
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
        final Episode item = episodeList.get(position);
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

        public void bindView(Episode episode) {
            if (!TextUtils.isEmpty(episode.image)) {

                Picasso.with(mContext)
                        .load(episode.image)
                        .into(posterImg);
            }

            animeTitleTv.setText(episode.title);
            Log.i("Episode name", episode.name);
            if (episode.name.contains("Tập")) {
                if (episode.name.contains("-")) {
                    episodeTitleTv.setText(episode.name.substring(0, episode.name.indexOf("-")).trim());
                } else {
                    episodeTitleTv.setText(episode.name.substring(0, 5).trim());
                }
            } else {
                episodeTitleTv.setText(episode.name);
            }

        }
    }

    public interface OnLatestEpisodeAdapterListener {
        void onItemClick(Episode item, int position);
    }
}
