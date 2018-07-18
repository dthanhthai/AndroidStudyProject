package com.example.doanthanhthai.mangafox.parser;

import android.util.Log;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.share.Constant;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by DOAN THANH THAI on 7/15/2018.
 */

public class AnimeParser {
    private static final String TAG = AnimeParser.class.getSimpleName();

//    public static List<Anime> getListAnimeByEpisode(String url) {
//        ArrayList<Anime> animeList = new ArrayList<>();
//        Document document = null;
//        try {
//            document = (Document) Jsoup.connect(url).get();
//
//            if (document != null) {
//                Elements subjectElements = document.select("div.tray-item");
//                if (subjectElements != null && subjectElements.size() > 0) {
//                    for (Element element : subjectElements) {
//                        Element infoElement = element.getElementsByTag("a").first();
//                        Log.i(TAG, "Link: " + infoElement.attr("href"));
//                        Anime anime = new Anime();
//                        anime.episode = new Episode();
//                        String rawUrl = Constant.HOME_URL + infoElement.attr("href");
//
//                        StringTokenizer st = new StringTokenizer(rawUrl, "/");
//                        List<String> rawLinkItems = new ArrayList<>();
//                        while (st.hasMoreTokens()) {
//                            rawLinkItems.add(st.nextToken());
//                        }
//                        anime.url = rawLinkItems.get(0) + "//" + rawLinkItems.get(1) + "/" + rawLinkItems.get(2);
//                        anime.episode.url = rawUrl;
//
//                        st = new StringTokenizer(rawLinkItems.get(3), "-");
//                        List<String> nameItems = new ArrayList<>();
//                        while (st.hasMoreTokens()) {
//                            nameItems.add(st.nextToken());
//                        }
//                        try {
//                            anime.episode.curNum = Integer.parseInt(nameItems.get(1));
//                        } catch (NumberFormatException ex) {
//                            anime.episode.curNum = -1;
//                            Log.e(TAG, ex.getMessage());
//                        }
//
//
//                        if (infoElement != null) {
//                            Element imageSubject = infoElement.getElementsByClass("tray-item-thumbnail").first();
//                            Element descriptionSubject = infoElement.getElementsByClass("tray-item-description").first();
//                            Element upcomingSubject = infoElement.getElementsByClass("tray-item-upcoming").first();
//                            Element rankSubject = infoElement.getElementsByClass("tray-item-rank").first();
//
//                            //Don't add upcoming episode into list
//                            if (upcomingSubject != null) {
//                                continue;
//                            }
//                            if (imageSubject != null) {
//                                anime.image = imageSubject.attr("src");
//                            }
//                            if (descriptionSubject != null) {
//                                Element titleSubject = descriptionSubject.getElementsByClass("tray-item-title").first();
//                                if (titleSubject != null) {
//                                    anime.title = titleSubject.text();
//                                }
//                                Element nameSubject = descriptionSubject.getElementsByClass("tray-item-meta-info").first().getElementsByTag("span").first();
//                                if (nameSubject != null) {
//                                    anime.episode.name = nameSubject.text();
//
//                                    //Get episode number
////                                        if (anime.episode.name.contains("Tập")) {
////                                            if (anime.episode.name.contains("-")) {
////                                                String tmp = anime.episode.name.substring(0, anime.episode.name.indexOf("-")).trim();
////                                                anime.episode.curNum = Integer.parseInt(tmp.toLowerCase().replace("tập", "").trim());
////                                            }
////                                        } else {
////                                            anime.episode.curNum = -1;
////                                        }
//                                }
//                            }
//
//                            if (rankSubject != null) {
//                                Element rankNumSubject = infoElement.getElementsByTag("div").first();
//                                anime.rank = rankNumSubject != null ? Integer.parseInt(rankNumSubject.text()) : -1;
//                            }
//
//                            animeList.add(anime);
//                        }
//                    }
//                    Log.i(TAG, "List count: " + animeList.size());
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Parse date fail: " + e.getMessage());
//        }
//        return animeList;
//    }

    public static List<Anime> getListAnimeItem(Document document) {
        ArrayList<Anime> animeList = new ArrayList<>();
        if (document != null) {
            Elements subjectElements = document.select("div.ah-row-film>div.ah-col-film");
            if (subjectElements != null && subjectElements.size() > 0) {
                for (Element element : subjectElements) {
                    Anime anime = new Anime();
                    anime.episode = new Episode();
                    Element padSubject = element.getElementsByClass("ah-pad-film").first();
                    if (padSubject != null) {
                        anime.url = padSubject.getElementsByTag("a").first().attr("href");
                        anime.image = padSubject.getElementsByTag("img").first().attr("src");
                        anime.episodeInfo = padSubject.getElementsByClass("number-ep-film").first().text();
                        anime.rate = padSubject.getElementsByClass("rate-point").first() != null ? padSubject.getElementsByClass("rate-point").first().text() : "";
                        anime.title = padSubject.getElementsByClass("name-film").first().text();
                        Log.i(TAG, "Link film: " + anime.url);

                    }
                    animeList.add(anime);
                }
                Log.i(TAG, "List count: " + animeList.size());
            }
        }
        return animeList;
    }

    public static List<Anime> getListBannerAnime(Document document) {
        ArrayList<Anime> animeList = new ArrayList<>();
        if (document != null) {
            Elements subjectElements = document.select("div.ah-home-fnom>div.ah-col-film");
            if (subjectElements != null && subjectElements.size() > 0) {
                for (Element element : subjectElements) {
                    Anime anime = new Anime();
                    anime.episode = new Episode();
                    Element padSubject = element.getElementsByClass("ah-pad-film").first();
                    if (padSubject != null) {
                        anime.url = padSubject.getElementsByTag("a").first().attr("href");
                        anime.bannerImage = padSubject.getElementsByTag("img").first().attr("src");
                        anime.episodeInfo = padSubject.getElementsByClass("number-ep-film").first().text();
                        anime.rate = padSubject.getElementsByClass("rate-point").first().text();
                        anime.title = padSubject.getElementsByClass("name-film").first().getElementsByTag("span").get(1).text();
                        anime.year = Integer.parseInt(padSubject.getElementsByClass("name-film").first().getElementsByTag("span").get(2).text());
                        Log.i(TAG, "Link film: " + anime.url);

                    }
                    animeList.add(anime);
                }
                Log.i(TAG, "List count: " + animeList.size());
            }
        }
        return animeList;
    }


    public static List<Anime> getListAnimeByItem(String url) {
        ArrayList<Anime> listEpisode = new ArrayList<>();
        Document document = null;
        try {
            document = (Document) Jsoup.connect(url).get();

            if (document != null) {
                Elements subjectElements = document.select("div.tray-item");
                if (subjectElements != null && subjectElements.size() > 0) {
                    for (Element element : subjectElements) {
                        Element infoElement = element.getElementsByTag("a").first();
                        Log.i(TAG, "Link: " + infoElement.attr("href"));
                        Anime anime = new Anime();
                        anime.episode = new Episode();
                        anime.url = Constant.HOME_URL + infoElement.attr("href");
                        anime.episode.url = Constant.HOME_URL + infoElement.attr("href");

                        if (infoElement != null) {
                            Element imageSubject = infoElement.getElementsByClass("tray-item-thumbnail").first();
                            Element descriptionSubject = infoElement.getElementsByClass("tray-item-description").first();
                            if (imageSubject != null) {
                                anime.image = imageSubject.attr("src");
                            }
                            if (descriptionSubject != null) {
                                Element titleSubject = descriptionSubject.getElementsByClass("tray-item-title").first();
                                if (titleSubject != null) {
                                    anime.title = titleSubject.text();
                                }
                                Element episodeInfoSubject = descriptionSubject.select("div.tray-film-update").first();
                                if (episodeInfoSubject != null) {
                                    anime.episodeInfo = episodeInfoSubject.text();
                                }
                            }
                            listEpisode.add(anime);
                        }
                    }
                    Log.i(TAG, "List count: " + listEpisode.size());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return listEpisode;
    }
}
