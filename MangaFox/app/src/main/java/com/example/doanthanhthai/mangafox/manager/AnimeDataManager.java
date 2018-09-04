package com.example.doanthanhthai.mangafox.manager;

import android.graphics.Bitmap;

import com.example.doanthanhthai.mangafox.model.Anime;

import java.util.List;

public class AnimeDataManager {
    private static final String TAG = AnimeDataManager.class.getSimpleName();

    private static AnimeDataManager instance;
    private Anime anime;
    private List<Anime> favoriteAnimeList;
    private int indexFavoriteItem = -1;
    private Bitmap thumbnailBitmap;
    private Bitmap coverBitmap;
    private List<Anime> bannerList;
    private List<Anime> homeList;
    private int homeTotalPage;

    public static AnimeDataManager getInstance() {
        if (instance == null) {
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

    public boolean addFavoriteAnime(Anime favoriteAnime) {
        if (favoriteAnimeList != null) {
            favoriteAnimeList.add(favoriteAnime);
            return true;
        }
        return false;
    }

    public boolean removeFavoriteAnime(int indexFavoriteItem) {
        if (favoriteAnimeList != null) {
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

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public void resetThumbnailBitmap() {
        this.thumbnailBitmap = null;
    }

    public Bitmap getCoverBitmap() {
        return coverBitmap;
    }

    public void setCoverBitmap(Bitmap coverBitmap) {
        this.coverBitmap = coverBitmap;
    }

    public void resetCoverBitmap() {
        this.coverBitmap = null;
    }

    public List<Anime> getBannerList() {
        return bannerList;
    }

    public void setBannerList(List<Anime> bannerList) {
        this.bannerList = bannerList;
    }

    public List<Anime> getHomeList() {
        return homeList;
    }

    public void setHomeList(List<Anime> homeList) {
        this.homeList = homeList;
    }

    public int getHomeTotalPage() {
        return homeTotalPage;
    }

    public void setHomeTotalPage(int homeTotalPage) {
        this.homeTotalPage = homeTotalPage;
    }
}
