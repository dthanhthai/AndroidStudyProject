package com.example.doanthanhthai.mangafox.parser

import android.webkit.WebView
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.model.Category
import org.jsoup.nodes.Document

/**
 * Created by ThaiDT1 on 7/23/2018.
 */

interface IBaseAnimeParser {
    fun getAnimeDetail(document: Document?, curAnime: Anime): Anime?
    fun getListAnimeItem(document: Document?): List<Anime>
    fun getListBannerAnime(document: Document?): List<Anime>
    fun getPaginationAnime(document: Document?): Int
    fun getDirectLinkPlayer(document: Document, webView: WebView, curAnime: Anime, indexPlayingItem: Int): Anime?
    fun getDirectLinkDetail(document: Document, webView: WebView, curAnime: Anime): Anime?
    fun getListAnimeGenre(document: Document?): List<Category>
    fun getListCNGenre(document: Document?): List<Category>
    fun getYearGenre(document: Document?): List<Category>
    fun getListCartoon(document: Document?): List<Category>
}
