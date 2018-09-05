package com.example.doanthanhthai.mangafox.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.io.Serializable

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

class Anime : Serializable {

    var url: String? = null
    var title: String? = null
    var orderTitle: String? = null
    var image: String? = null
    var bannerImage: String? = null
    var coverImage: String? = null
    var episodeInfo: String? = null
    var rate: String? = null
    var year: Int = 0
    var description: String? = null
    var genres: String? = null
    var duration: String? = null
    var newEpisodeInfo: String? = null
    lateinit var episodeList: MutableList<Episode>
    var isFavorite: Boolean? = false
    var relatedContents: MutableList<RelatedContent>? = null
}
