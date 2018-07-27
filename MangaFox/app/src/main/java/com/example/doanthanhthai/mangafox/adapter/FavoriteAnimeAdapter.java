package com.example.doanthanhthai.mangafox.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.R;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.share.DynamicColumnHelper;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class FavoriteAnimeAdapter extends RecyclerView.Adapter<FavoriteAnimeAdapter.FavoriteAnimeViewHolder> {
    private List<Anime> animeList;
    private OnFavoriteAnimeAdapterListener mListener;
    private int widthItem = -1;
    private int spacing = -1;

    public FavoriteAnimeAdapter(OnFavoriteAnimeAdapterListener listener) {
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

    public void removeItemByIndex(int index) {
        animeList.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, animeList.size());
    }

    @Override
    public FavoriteAnimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new FavoriteAnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoriteAnimeViewHolder holder, final int position) {
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

    public class FavoriteAnimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView posterImg;
        TextView animeTitleTv;
        TextView animeEpisodeInfoTv;
        ImageView moreOptionIv;
        Context mContext;
        int position;

        public ImageView getPosterImg() {
            return posterImg;
        }

        public TextView getAnimeTitleTv() {
            return animeTitleTv;
        }

        public FavoriteAnimeViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            posterImg = itemView.findViewById(R.id.anime_poster);
            animeTitleTv = itemView.findViewById(R.id.anime_title);
            animeEpisodeInfoTv = itemView.findViewById(R.id.episode_info);
            moreOptionIv = itemView.findViewById(R.id.anime_more_option);

            moreOptionIv.setOnClickListener(this);
        }

        public void bindView(Anime anime, int position) {
            this.position = position;
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
            animeEpisodeInfoTv.setText(anime.getEpisodeInfo());
            Log.i("Anime result name: ", anime.getTitle());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.anime_more_option) {
                createPopupMenu(v);
            }
        }

        private void createPopupMenu(final View view) {
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(view.getContext(), moreOptionIv);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.favorite_poupup_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Toast.makeText(view.getContext(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    removeItemByIndex(position);
                    AnimeDataManager.getInstance().setFavoriteAnimeList(animeList);
                    PreferenceHelper.getInstance(view.getContext()).saveListFavoriteAnime(animeList);
                    return true;
                }
            });

            popup.show();
        }

    }

    public interface OnFavoriteAnimeAdapterListener {
        void onItemClick(Anime item, int position);
    }
}
