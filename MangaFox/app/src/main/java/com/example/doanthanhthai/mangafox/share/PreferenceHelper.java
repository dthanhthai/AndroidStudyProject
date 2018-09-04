package com.example.doanthanhthai.mangafox.share;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DOAN THANH THAI on 6/21/2017.
 */

public class PreferenceHelper {
    private static final String TAG = PreferenceHelper.class.getSimpleName();
    private static final String NAME = "AnimeApp";
    private static final String FAVORITE_ANIMES = "favorite_anime";
    private static final String NIGHT_MODE = "is_night_mode";
    private static final String COOKIE = "cookie";

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

    public void saveCookie(Map<String, String> cookies) {
        SharedPreferences.Editor editor = mSharePreferences.edit();
        String json = new Gson().toJson(cookies);
        editor.putString(COOKIE, json);
        editor.apply();
    }

    public Map<String, String> getCookie() {
        Map<String, String> cookies = new HashMap<>();
        try {
            String json = mSharePreferences.getString(COOKIE, "");
            if (!TextUtils.isEmpty(json)) {
                cookies = (new Gson()).fromJson(json, new TypeToken<Map<String, String>>() {
                }.getType());
                if (cookies.get("check_vn") == null) {
                    cookies.put("check_vn", "1");
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            return cookies;
        }
        return cookies;
    }

    public void saveNightMode(boolean isNightMode) {
        SharedPreferences.Editor editor = mSharePreferences.edit();
        editor.putBoolean(NIGHT_MODE, isNightMode);
        editor.apply();
    }

    public boolean getNightMode(){
        return mSharePreferences.getBoolean(NIGHT_MODE, false);
    }
}
