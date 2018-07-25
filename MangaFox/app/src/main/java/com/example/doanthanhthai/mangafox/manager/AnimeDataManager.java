package com.example.doanthanhthai.mangafox.manager;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;

import java.util.List;

public class AnimeDataManager {
    private static final String TAG = AnimeDataManager.class.getSimpleName();

    private static AnimeDataManager instance;
    private Anime anime;
    private List<Anime> favoriteAnimeList;
    private int indexFavoriteItem = -1;
    private Bitmap bitmapDrawable;

    public static AnimeDataManager getInstance(){
        if(instance == null){
            instance = new AnimeDataManager();
        }
        return instance;
    }

    public Anime getAnime() {
        return anime;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public List<Anime> getFavoriteAnimeList() {
        return favoriteAnimeList;
    }

    public void setFavoriteAnimeList(List<Anime> favoriteAnimeList) {
        this.favoriteAnimeList = favoriteAnimeList;
    }

    public boolean addFavoriteAnime(Anime favoriteAnime){
        if(favoriteAnimeList != null){
            favoriteAnimeList.add(favoriteAnime);
            return true;
        }
        return false;
    }

    public boolean removeFavoriteAnime(int indexFavoriteItem){
        if(favoriteAnimeList != null){
            favoriteAnimeList.remove(indexFavoriteItem);
            return true;
        }
        return false;
    }

    public int getIndexFavoriteItem() {
        return indexFavoriteItem;
    }

    public void setIndexFavoriteItem(int indexFavoriteItem) {
        this.indexFavoriteItem = indexFavoriteItem;
    }

    public void resetIndexFavoriteItem() {
        this.indexFavoriteItem = -1;
    }

    public Bitmap getBitmapDrawable() {
        return bitmapDrawable;
    }

    public void setBitmapDrawable(Bitmap bitmapDrawable) {
        this.bitmapDrawable = bitmapDrawable;
    }
}
