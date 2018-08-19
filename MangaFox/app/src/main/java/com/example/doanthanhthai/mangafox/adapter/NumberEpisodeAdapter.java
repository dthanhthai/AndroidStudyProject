package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.model.Episode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class NumberEpisodeAdapter extends RecyclerView.Adapter<NumberEpisodeAdapter.NumberEpisodeViewHolder> {
    private List<Episode> episodeList;
    private OnNumberEpisodeAdapterListener mListener;
    private int currentNum = 1;
    private String curEpisodeName;

    public NumberEpisodeAdapter(OnNumberEpisodeAdapterListener listener) {
        this.episodeList = new ArrayList<>();
        mListener = listener;
    }

    public void setEpisodeList(List<Episode> episodeList) {
        this.episodeList = episodeList;
        notifyDataSetChanged();
    }

    public void setCurrentNum(String name) {
        this.curEpisodeName = name;
    }

    @Override
    public NumberEpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_number_episode, parent, false);
        return new NumberEpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NumberEpisodeViewHolder holder, final int position) {
        final Episode item = episodeList.get(position);
        holder.bindView(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(item, position);
                    curEpisodeName = item.getName();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public class NumberEpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView numnerEpisodeTv;
        FrameLayout wrapperLayout;
        Context mContext;

        public NumberEpisodeViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            numnerEpisodeTv = itemView.findViewById(R.id.number_episode_tv);
            wrapperLayout = itemView.findViewById(R.id.number_episode_layout);

        }

        public void bindView(Episode episode) {
            numnerEpisodeTv.setText(episode.getName());
            if (curEpisodeName.equals(episode.getName())) {
                wrapperLayout.setBackgroundColor(mContext.getResources().getColor(R.color.cyan));
                itemView.setEnabled(false);
            } else if (!TextUtils.isEmpty(episode.getDirectUrl())) {
                wrapperLayout.setBackgroundColor(mContext.getResources().getColor(R.color.numEpViewedColor));
                itemView.setEnabled(true);
            } else {
                wrapperLayout.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
                itemView.setEnabled(true);
            }
            Log.i("Episode episode: ", episode.getName() + "");
        }
    }

    public interface OnNumberEpisodeAdapterListener {
        void onItemClick(Episode item, int position);
    }
}
