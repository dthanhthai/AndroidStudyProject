package com.example.doanthanhthai.mangafox.parser;

import android.util.Log;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/17/2018.
 */

public class AnimeDetailParser {
    private static final String TAG = AnimeDetailParser.class.getSimpleName();

    public static Anime getAnimeDetail(Document document, Anime curAnime) {
        if (document != null) {
            Element thumbnailSubject = document.select("div.ah-pif-fthumbnail>img").first();
            Element coverSubject = document.select("div.ah-pif-fcover>img").first();
            Element rateSubject = document.select("div.ah-rate-film>span").first();
            Elements genresSubject = document.select("div.ah-pif-fdetails>ul>li>span");
            Element descriptionSubject = document.select("div.ah-pif-fcontent>p").first();
            Elements detailSubject = document.select("div.ah-pif-fdetails>ul>li");


            if (thumbnailSubject != null) {
                curAnime.image = thumbnailSubject.attr("src");
            }

            if (coverSubject != null) {
                curAnime.coverImage = coverSubject.attr("src");
            }

            if (rateSubject != null) {
                curAnime.rate = rateSubject.text();
            }

            if (genresSubject != null && genresSubject.size() > 0) {
                curAnime.genres = "";
                for (int i = 0; i < genresSubject.size(); i++) {
                    if (i == (genresSubject.size() - 1)) {
                        curAnime.genres += genresSubject.get(i).text();
                    } else {
                        curAnime.genres += (genresSubject.get(i).text() + ", ");
                    }
                }
            }

            if (descriptionSubject != null) {
                curAnime.description = descriptionSubject.text();
            }

            if (detailSubject != null && detailSubject.size() > 0) {
                String yearRaw = detailSubject.get(1).text();
                String durationRaw = detailSubject.get(3).text();
                try {
                    curAnime.year = Integer.parseInt(yearRaw.substring(yearRaw.indexOf(":") + 1).trim());
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    Log.e(TAG, ex.getMessage());
                    curAnime.year = -1;
                }
                curAnime.duration = yearRaw.substring(yearRaw.indexOf(":") + 1).trim();
            }
        }
        return curAnime;
    }
}
