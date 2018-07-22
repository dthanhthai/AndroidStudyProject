package com.example.doanthanhthai.mangafox.parser;

import android.text.TextUtils;
import android.webkit.WebView;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AnimePlayerParser {
    private static final String TAG = AnimePlayerParser.class.getSimpleName();

    public static Anime getDirectLinkPlayer(Document document, WebView webView, Anime curAnime, int indexPlayingItem) {
        Element videoSubject = document.selectFirst("div.film-player>div#ah-player>video>source");
        Elements fullNameSubject = document.select("div.ah-wf-title>h1");


        Episode episode = curAnime.episodeList.get(indexPlayingItem);

        if (videoSubject != null) {
            episode.directUrl = videoSubject.attr("src");
        }

        //Call to get html source code until we have right direct link
        String directLinkRaw = episode.directUrl;
        if (TextUtils.isEmpty(directLinkRaw)
                || (!TextUtils.isEmpty(directLinkRaw) && directLinkRaw.contains("media.yomedia.vn"))) {
            webView.loadUrl(
                    "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
            return null;
        }

        if (fullNameSubject != null) {
            episode.fullName = fullNameSubject.text();
        } else {
            episode.fullName = episode.name;
        }
        return curAnime;
    }

    public static Anime getDirectLinkDetail(Document document, WebView webView, Anime curAnime) {
        Element videoSubject = document.selectFirst("div.film-player>div#ah-player>video>source");
        Elements listEpisodeSubject = document.select("div.ah-wf-le>ul>li>a");
        Elements fullNameSubject = document.select("div.ah-wf-title>h1");

        Episode firstEpisode = curAnime.episodeList.get(0);

        if (videoSubject != null) {
            firstEpisode.directUrl = videoSubject.attr("src");
        }

        //Call to get html source code until we have right direct link
        String directLinkRaw = firstEpisode.directUrl;
        if (TextUtils.isEmpty(directLinkRaw)
                || (!TextUtils.isEmpty(directLinkRaw) && directLinkRaw.contains("media.yomedia.vn"))) {
            webView.loadUrl(
                    "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
            return null;
        }

        //Get list firstEpisode information
        if (listEpisodeSubject != null && listEpisodeSubject.size() > 0) {
            for (int i = 0; i < listEpisodeSubject.size(); i++) {
                //The first firstEpisode is available -> just update data
                if (i == 0) {
                    firstEpisode.url = listEpisodeSubject.get(i).attr("href");
                    firstEpisode.name = listEpisodeSubject.get(i).text();
                    firstEpisode.fullName
                            = fullNameSubject != null ? fullNameSubject.text() : firstEpisode.name;
                } else {
                    Episode item = new Episode();
                    item.url = listEpisodeSubject.get(i).attr("href");
                    item.name = listEpisodeSubject.get(i).text();
                    curAnime.episodeList.add(item);
                }
            }
        }

        return curAnime;
    }
}
