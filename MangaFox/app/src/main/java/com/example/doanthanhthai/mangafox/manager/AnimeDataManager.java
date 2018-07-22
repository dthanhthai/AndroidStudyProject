package com.example.doanthanhthai.mangafox.manager;

import com.example.doanthanhthai.mangafox.model.Anime;

public class AnimeDataManager {
    private static final String TAG = AnimeDataManager.class.getSimpleName();

    private static AnimeDataManager instance;
    private Anime anime;

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
}
