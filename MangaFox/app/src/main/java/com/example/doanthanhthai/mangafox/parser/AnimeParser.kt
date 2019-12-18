package com.example.doanthanhthai.mangafox.parser

import android.text.TextUtils
import android.util.Log
import android.webkit.WebView

import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.model.Category
import com.example.doanthanhthai.mangafox.model.Episode
import com.example.doanthanhthai.mangafox.model.RelatedContent
import com.example.doanthanhthai.mangafox.share.Constant.HOME_URL
import com.example.doanthanhthai.mangafox.share.Constant.RELATED_URL
import com.example.doanthanhthai.mangafox.share.Utils

import org.jsoup.nodes.Document

import java.util.ArrayList

/**
 * Created by DOAN THANH THAI on 7/15/2018.
 */

class AnimeParser : IBaseAnimeParser {
    companion object {
        private val TAG = AnimeParser::class.java.simpleName
    }

    init {

    }

    override fun getAnimeDetail(document: Document?, curAnime: Anime): Anime? {
        if (document != null) {
            val thumbnailSubject = document.select("div.ah-pif-fthumbnail>img").first()
            val coverSubject = document.select("div.ah-pif-fcover>img").first()
//            val rateSubject = document.select("div.ah-rate-film>span").first()
            val genresSubject = document.select("div.ah-pif-fdetails>ul>li>span")
            val descriptionSubject = document.select("div.ah-pif-fcontent>p").first()
            val detailSubject = document.select("div.ah-pif-fdetails>ul>li")
            val buttonSubject = document.selectFirst("div.ah-pif-ftool>div.ah-float-left>span>a")
            val relatedSubject = document.select("div.ah-pif-relation>a")
            val titleSubject = document.selectFirst("h1>a")


            //Document don't have thumbnail -> is not anime page, that is confirm page
            if (thumbnailSubject != null) {
                curAnime.image = thumbnailSubject.attr("src")
            } else {
                return null
            }

            titleSubject?.let {
                curAnime.title = it.text()
                curAnime.url = it.attr("href")
            }

            if (coverSubject != null) {
                curAnime.coverImage = coverSubject.attr("src")
            }

//            if (rateSubject != null) {
//                curAnime.rate = rateSubject.text()
//            }

            if (genresSubject != null && genresSubject.size > 0) {
                curAnime.genres = ""
                for (i in genresSubject.indices) {
                    if (i == genresSubject.size - 1) {
                        curAnime.genres = curAnime.genres!! + genresSubject[i].text()
                    } else {
                        curAnime.genres = curAnime.genres!! + (genresSubject[i].text() + ", ")
                    }
                }
            }

            if (descriptionSubject != null) {
                curAnime.description = descriptionSubject.text()
            }

            if (detailSubject != null && detailSubject.size > 0) {

                for (element in detailSubject) {
                    if (element.text().contains("Năm phát hành")) {
                        val yearRaw = element.text()
                        try {
                            curAnime.year = Integer.parseInt(yearRaw.substring(yearRaw.indexOf(":") + 1).trim { it <= ' ' })
                        } catch (ex: NumberFormatException) {
                            ex.printStackTrace()
                            Log.e(TAG, ex.message)
                            curAnime.year = -1
                        }

                    } else if (element.text().contains("Thời lượng")) {
                        val durationRaw = element.text()
                        curAnime.duration = durationRaw.substring(durationRaw.indexOf(":") + 1).trim { it <= ' ' }
                    }
//                    else if (element.text().contains("Tên khác")) {
//                        val orderTitleRaw = element.text()
//                        curAnime.orderTitle = orderTitleRaw.substring(orderTitleRaw.indexOf(":") + 1).trim { it <= ' ' }
//                    }
                    else if (element.text().contains("Tập mới")) {
                        val newEpisodeRaw = element.text()
                        curAnime.newEpisodeInfo = newEpisodeRaw.substring(newEpisodeRaw.indexOf(":") + 1).trim { it <= ' ' }
                    }
                }
            }

            if (relatedSubject != null && relatedSubject.size > 0) {
                var relatedContentList = mutableListOf<RelatedContent>()
                for (element in relatedSubject) {
                    val relatedContent = RelatedContent()
                    relatedContent.name = element.text()
                    relatedContent.url = RELATED_URL + element.attr("href")
                    if (!Utils.isTextEmpty(element.className())) {
                        relatedContent.isCurrent = true
                    }
                    relatedContentList.add(relatedContent)
                }
                curAnime.relatedContents = relatedContentList
            }

            if (buttonSubject != null) {
                val episodes = ArrayList<Episode>()
                val item = Episode()
                item.name = "1"
                item.url = buttonSubject.attr("href")
                episodes.add(item)
                curAnime.episodeList = episodes
            }
        }
        return curAnime
    }

    override fun getDirectLinkPlayer(document: Document, webView: WebView, curAnime: Anime, indexPlayingItem: Int): Anime? {
        val videoSubject = document.selectFirst("div.film-player>div#ah-player>video>source")
        val fullNameSubject = document.select("div.ah-wf-name>h1")


        val episode = curAnime.episodeList[indexPlayingItem]

        if (videoSubject != null) {
            episode.directUrl = videoSubject.attr("src")
        }

        //Call to get html source code until we have right direct link
        val directLinkRaw = episode.directUrl
        if (TextUtils.isEmpty(directLinkRaw) || !TextUtils.isEmpty(directLinkRaw) && directLinkRaw!!.contains("media.yomedia.vn")) {
            webView.loadUrl(
                    "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);")
            return null
        }

        if (fullNameSubject != null) {
            episode.fullName = fullNameSubject.text()
        } else {
            episode.fullName = episode.name
        }
        return curAnime
    }

    override fun getDirectLinkDetail(document: Document, webView: WebView, curAnime: Anime): Anime? {
//        val videoSubject = document.selectFirst("div.film-player>div#ah-player>video>source")
        val videoSubject = document.selectFirst("div.film-player>div#ah-player>div#iframeA>iframe")
        val listEpisodeSubject = document.select("div.ah-wf-le>ul>li>a")
        val fullNameSubject = document.select("div.ah-wf-title>h1")

        val firstEpisode = curAnime.episodeList[0]

        if (videoSubject != null) {
            firstEpisode.directUrl = videoSubject.attr("src")
        }

        //Call to get html source code until we have right direct link
        val directLinkRaw = firstEpisode.directUrl
        if (TextUtils.isEmpty(directLinkRaw) || !TextUtils.isEmpty(directLinkRaw) && directLinkRaw!!.contains("media.yomedia.vn")) {
            webView.loadUrl(
                    "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);")
            return null
        }

        //Get list firstEpisode information
        if (listEpisodeSubject != null && listEpisodeSubject.size > 0) {
            for (i in listEpisodeSubject.indices) {
                //The first firstEpisode is available -> just update data
                if (i == 0) {
                    firstEpisode.url = listEpisodeSubject[i].attr("href")
                    firstEpisode.name = listEpisodeSubject[i].text()
                    firstEpisode.fullName = if (fullNameSubject != null) fullNameSubject.text() else firstEpisode.name
                } else {
                    val item = Episode()
                    item.url = listEpisodeSubject[i].attr("href")
                    item.name = listEpisodeSubject[i].text()
//                    (curAnime.episodeList as ArrayList).add(item)
                    curAnime.episodeList.add(item)
                }
            }
        }

        return curAnime
    }

    override fun getListAnimeItem(document: Document?): List<Anime> {
        val animeList = ArrayList<Anime>()
        if (document != null) {
            val subjectElements = document.select("div.ah-row-film>div.ah-col-film")
            if (subjectElements != null && subjectElements.size > 0) {
                for (element in subjectElements) {
                    val anime = Anime()
                    //                    anime.episode = new Episode();
                    val padSubject = element.getElementsByClass("ah-pad-film").first()
                    if (padSubject != null) {
                        anime.url = padSubject.getElementsByTag("a").first().attr("href")
                        anime.image = padSubject.getElementsByTag("img").first().attr("src")
                        anime.episodeInfo = padSubject.getElementsByClass("number-ep-film").first().text()
//                        anime.rate = if (padSubject.getElementsByClass("rate-point").first() != null) padSubject.getElementsByClass("rate-point").first().text() else ""
                        anime.rate = padSubject.getElementsByClass("rate-point").first()?.let { it.text() } ?: run { "" }
                        anime.title = padSubject.getElementsByClass("name-film").first().text()
                        Log.i(TAG, "Link film: " + anime.url!!)

                    }
                    animeList.add(anime)
                }
                Log.i(TAG, "List count: " + animeList.size)
            }
        }
        return animeList
    }

    override fun getListBannerAnime(document: Document?): List<Anime> {
        val animeList = ArrayList<Anime>()
        if (document != null) {
            val subjectElements = document.select("div.ah-home-fnom>div.ah-col-film")
            if (subjectElements != null && subjectElements.size > 0) {
                for (element in subjectElements) {
                    val anime = Anime()
                    //                    anime.episode = new Episode();
                    val padSubject = element.getElementsByClass("ah-pad-film").first()
                    if (padSubject != null) {
                        anime.url = padSubject.getElementsByTag("a").first().attr("href")
                        anime.bannerImage = padSubject.getElementsByTag("img").first().attr("src")
                        anime.episodeInfo = padSubject.getElementsByClass("number-ep-film").first().text()
                        anime.rate = padSubject.getElementsByClass("rate-point").first().text()
                        anime.title = padSubject.getElementsByClass("name-film").first().getElementsByTag("span")[1].text()
                        anime.year = Integer.parseInt(padSubject.getElementsByClass("name-film").first().getElementsByTag("span")[2].text())
                        Log.i(TAG, "Link film: " + anime.url!!)

                    }
                    animeList.add(anime)
                }
                Log.i(TAG, "List count: " + animeList.size)
            }
        }
        return animeList
    }

    override fun getPaginationAnime(document: Document?): Int {
        val animeList = ArrayList<Anime>()
        var totalPage = 1
        if (document != null) {
            val subjectElements = document.select("div.ah-pagenavi>ul.pagination>li>a")
            if (subjectElements != null && subjectElements.size > 0) {
                try {
                    totalPage = Integer.parseInt(subjectElements[subjectElements.size - 1].text())
                } catch (e: NumberFormatException) {
                    Log.e(TAG, e.message)
                    return 1
                }

                Log.i(TAG, "List pagination: " + animeList.size)
            }
        }
        return totalPage
    }

    override fun getListAnimeGenre(document: Document?): List<Category> {
        val genreList = ArrayList<Category>()
        document?.let {
            val parentElement = it.select("ul.ah-ulsm>li>a.non-routed")
            val allElement = it.select("ul.ah-ulsm>li")
            var isIgnoreValue = true
            if (allElement != null && !allElement.isEmpty()
                    && parentElement != null && !parentElement.isEmpty()) {
                for (element in allElement) {
                    if (element.text().equals(parentElement.get(0).text())) {
                        isIgnoreValue = false
                        continue
                    }
                    if (!isIgnoreValue) {
                        if (element.text().equals(parentElement.get(1).text())) {
                            break
                        }
                        var category = Category()
                        category.name = element.text()
                        category.url = element.getElementsByTag("a").first().attr("href")
                        if (category.name.equals("ecchi", ignoreCase = true)) {
                            genreList.add(0, category)
                        } else {
                            genreList.add(category)
                        }
                        Log.i(TAG, "Genre link: " + category.url)

                    }
                }
            }
        }
        Log.i(TAG, "List genre: " + genreList.size)
        return genreList
    }

    override fun getListCNGenre(document: Document?): List<Category> {
        val genreList = ArrayList<Category>()
        document?.let {
            val parentElement = it.select("ul.ah-ulsm>li>a.non-routed")
            val allElement = it.select("ul.ah-ulsm>li")
            var isIgnoreValue = true
            if (allElement != null && !allElement.isEmpty()
                    && parentElement != null && !parentElement.isEmpty()) {
                for (element in allElement) {
                    if (element.text().equals(parentElement.get(1).text())) {
                        isIgnoreValue = false
                        continue
                    }
                    if (!isIgnoreValue) {
                        if (element.text().equals(parentElement.get(2).text())) {
                            break
                        }

                        var category = Category()
                        category.name = element.text()
                        category.url = element.getElementsByTag("a").first().attr("href")
                        genreList.add(category)
                        Log.i(TAG, "Genre link: " + category.url)

                    }
                }
            }
        }
        Log.i(TAG, "List genre: " + genreList.size)
        return genreList
    }

    override fun getYearGenre(document: Document?): List<Category> {
        val genreList = ArrayList<Category>()
        document?.let {
            val parentElement = it.select("ul.ah-ulsm>li>a.non-routed")
            val allElement = it.select("ul.ah-ulsm>li")
            var isIgnoreValue = true
            if (allElement != null && !allElement.isEmpty()
                    && parentElement != null && !parentElement.isEmpty()) {
                for (element in allElement) {
                    if (element.text().equals(parentElement.get(3).text())) {
                        isIgnoreValue = false
                        continue
                    }
                    if (!isIgnoreValue) {
                        if (element.text().equals(parentElement.get(4).text())) {
                            break
                        }

                        var category = Category()
                        category.name = element.text()
                        category.url = element.getElementsByTag("a").first().attr("href")
                        genreList.add(category)
                        Log.i(TAG, "Genre link: " + category.url)

                    }
                }
            }
        }
        Log.i(TAG, "List genre: " + genreList.size)
        return genreList
    }

    override fun getListCartoon(document: Document?): List<Category> {
        val genreList = ArrayList<Category>()
        document?.let {
            val parentElement = it.select("ul.ah-ulsm>li>a.non-routed")
            val allElement = it.select("ul.ah-ulsm>li")
            var isIgnoreValue = true
            if (allElement != null && !allElement.isEmpty()
                    && parentElement != null && !parentElement.isEmpty()) {
                for (element in allElement) {
                    if (element.text().equals(parentElement.get(2).text())) {
                        isIgnoreValue = false
                        continue
                    }
                    if (!isIgnoreValue) {
                        if (element.text().equals(parentElement.get(3).text())) {
                            break
                        }

                        var category = Category()
                        category.name = element.text()
                        category.url = element.getElementsByTag("a").first().attr("href")
                        genreList.add(category)
                        Log.i(TAG, "Genre link: " + category.url)

                    }
                }
            }
        }
        Log.i(TAG, "List genre: " + genreList.size)
        return genreList
    }

}
