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
            Element buttonSubject = document.selectFirst("div.ah-pif-ftool>div.ah-float-left>span>a");

            //Document don't have thumbnail -> is not anime page, that is confirm page
            if (thumbnailSubject != null) {
                curAnime.setImage(thumbnailSubject.attr("src"));
            }else{
                return null;
            }

            if (coverSubject != null) {
                curAnime.setCoverImage(coverSubject.attr("src"));
            }

            if (rateSubject != null) {
                curAnime.setRate(rateSubject.text());
            }

            if (genresSubject != null && genresSubject.size() > 0) {
                curAnime.setGenres("");
                for (int i = 0; i < genresSubject.size(); i++) {
                    if (i == (genresSubject.size() - 1)) {
                        curAnime.setGenres(curAnime.getGenres() + genresSubject.get(i).text());
                    } else {
                        curAnime.setGenres(curAnime.getGenres() + (genresSubject.get(i).text() + ", "));
                    }
                }
            }

            if (descriptionSubject != null) {
                curAnime.setDescription(descriptionSubject.text());
            }

            if (detailSubject != null && detailSubject.size() > 0) {

                for (Element element : detailSubject) {
                    if (element.text().contains("Năm phát hành")) {
                        String yearRaw = element.text();
                        try {
                            curAnime.setYear(Integer.parseInt(yearRaw.substring(yearRaw.indexOf(":") + 1).trim()));
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                            Log.e(TAG, ex.getMessage());
                            curAnime.setYear(-1);
                        }
                    } else if (element.text().contains("Thời lượng")) {
                        String durationRaw = element.text();
                        curAnime.setDuration(durationRaw.substring(durationRaw.indexOf(":") + 1).trim());
                    } else if (element.text().contains("Tên khác")) {
                        String orderTitleRaw = element.text();
                        curAnime.setOrderTitle(orderTitleRaw.substring(orderTitleRaw.indexOf(":") + 1).trim());
                    } else if(element.text().contains("Tập mới")){
                        String newEpisodeRaw = element.text();
                        curAnime.setNewEpisodeInfo(newEpisodeRaw.substring(newEpisodeRaw.indexOf(":") + 1).trim());
                    }
                }
            }

            if(buttonSubject != null){
                List<Episode> episodes = new ArrayList<>();
                Episode item = new Episode();
                item.setName("1");
                item.setUrl(buttonSubject.attr("href"));
                episodes.add(item);
                curAnime.setEpisodeList(episodes);
            }
        }
        return curAnime;
    }
}
