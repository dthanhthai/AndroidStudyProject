package com.example.doanthanhthai.mangafox.repository

import android.webkit.WebView
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.model.Category
import com.example.doanthanhthai.mangafox.parser.AnimeParser
import com.example.doanthanhthai.mangafox.parser.IBaseAnimeParser
import org.jsoup.nodes.Document

/**
 * Created by ThaiDT1 on 7/24/2018.
 */

class AnimeRepository(type: WEB_TYPE) {

    enum class WEB_TYPE {
        ANIMEHAY,
        ANIVN
    }

    var baseParser: IBaseAnimeParser? = null

    init {
        when (type) {
            (WEB_TYPE.ANIMEHAY) -> baseParser = AnimeParser()
        }
    }

    fun getAnimeDetail(document: Document?, curAnime: Anime): Anime? {
        return baseParser?.getAnimeDetail(document, curAnime)
    }

    fun getListAnimeItem(document: Document?): List<Anime>? {
        return baseParser?.getListAnimeItem(document)
    }

    fun getListBannerAnime(document: Document?): List<Anime>? {
        return baseParser?.getListBannerAnime(document)
    }

    fun getPaginationAnime(document: Document?): Int? {
        return baseParser?.getPaginationAnime(document)
    }

    fun getDirectLinkPlayer(document: Document, webView: WebView, curAnime: Anime, indexPlayingItem: Int): Anime? {
        return baseParser?.getDirectLinkPlayer(document, webView, curAnime, indexPlayingItem)
    }

    fun getDirectLinkDetail(document: Document, webView: WebView, curAnime: Anime): Anime? {
        return baseParser?.getDirectLinkDetail(document, webView, curAnime)
    }

    fun getListAnimeGenre(document: Document?): List<Category>? {
        return baseParser?.getListAnimeGenre(document)
    }

    fun getListCNGenre(document: Document?): List<Category>? {
        return baseParser?.getListCNGenre(document)
    }

    fun getListYearGenre(document: Document?): List<Category>? {
        return baseParser?.getYearGenre(document)
    }

    fun getListCartoon(document: Document?): List<Category>? {
        return baseParser?.getListCartoon(document)
    }
}