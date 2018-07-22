package com.example.doanthanhthai.mangafox.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */
class Episode : Serializable {
    var url: String? = null
    var name: String? = null
    var fullName: String? = null
    var directUrl: String? = null
}
