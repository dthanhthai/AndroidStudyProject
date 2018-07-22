package com.example.doanthanhthai.mangafox.share;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 6/21/2017.
 */

public class PreferenceHelper {
    private static final String TAG = PreferenceHelper.class.getSimpleName();
    private static final String NAME = "AnimeApp";
    private static final String FAVORITE_ANIMES = "favorite_anime";

    private static PreferenceHelper mInstance;
    private SharedPreferences mSharePreferences;

    private PreferenceHelper(Context context) {
        mSharePreferences = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public synchronized static PreferenceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferenceHelper(context);
        }
        return mInstance;
    }

    public void saveListFavoriteAnime(List<Anime> favoriteList) {
        SharedPreferences.Editor editor = mSharePreferences.edit();
        String json = new Gson().toJson(favoriteList);
        editor.putString(FAVORITE_ANIMES, json);
        editor.apply();
    }

    public List<Anime> getListFavoriteAnime() {
        List<Anime> favoriteList = new ArrayList<>();
        try {
            String json = mSharePreferences.getString(FAVORITE_ANIMES, "");
            if (!TextUtils.isEmpty(json)) {
                favoriteList = (new Gson()).fromJson(json, new TypeToken<ArrayList<Anime>>() {
                }.getType());
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            return favoriteList;
        }
        return favoriteList;
    }
}
