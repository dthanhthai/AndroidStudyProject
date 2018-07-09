package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.doanthanhthai.mangafox.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class NumberEpisodeAdapter extends RecyclerView.Adapter<NumberEpisodeAdapter.NumberEpisodeViewHolder> {
    private List<Integer> episodeList;
    private OnNumberEpisodeAdapterListener mListener;
    private int currentNum = 1;

    public NumberEpisodeAdapter(OnNumberEpisodeAdapterListener listener) {
        this.episodeList = new ArrayList<>();
        mListener = listener;
    }

    public void setEpisodeList(List<Integer> episodeList) {
        this.episodeList = episodeList;
        notifyDataSetChanged();
    }

    public void setCurrentNum(int currentNum) {
        this.currentNum = currentNum;
        notifyDataSetChanged();
    }

    @Override
    public NumberEpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_number_episode, parent, false);
        return new NumberEpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NumberEpisodeViewHolder holder, final int position) {
        final int item = episodeList.get(position);
        holder.bindView(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(item, position);
                    currentNum = item;
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

        public void bindView(int anime) {
            numnerEpisodeTv.setText(anime + "");
            if(currentNum == anime){
                wrapperLayout.setBackgroundColor(mContext.getResources().getColor(R.color.cyan));
            }
            Log.i("Episode number: ", anime+"");
        }
    }

    public interface OnNumberEpisodeAdapterListener {
        void onItemClick(int item, int position);
    }
}
