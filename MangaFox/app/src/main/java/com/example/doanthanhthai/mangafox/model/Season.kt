package com.example.doanthanhthai.mangafox.model

import java.io.Serializable

/**
 * Created by ThaiDT1 on 7/17/2018.
 */

class Season : Serializable {
    var name: String? = null
    var url: String? = null
    var episodeList: List<Episode>? = null
}
