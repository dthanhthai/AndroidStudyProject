package com.example.doanthanhthai.mangafox.share

import android.content.Context
import com.example.doanthanhthai.mangafox.R

class DynamicColumnHelper(val context: Context) {

    var colNum: Int = 0;
    var spacing: Int = 0;
    var widthItem: Int = 0;

    init {
        val widthScreen = Utils.getScreenSize(context)[0]
        widthItem = context.resources.getDimension(R.dimen.min_width_item).toInt()

        colNum = widthScreen / widthItem
        var remain = widthScreen % widthItem

        if (colNum == 1) {
            colNum = 2
            spacing = context.resources.getDimension(R.dimen.min_spacing_item).toInt()
            widthItem = (widthScreen - spacing * 2 * colNum) / colNum
        } else {
            if (remain / (colNum * 2) > context.resources.getDimension(R.dimen.max_spacing_item) || remain / (colNum * 2) <= 0) {
                spacing = context.resources.getDimension(R.dimen.max_spacing_item).toInt()
            } else {
                spacing = remain / (colNum * 2)
            }
            remain -= spacing * (colNum * 2)
            widthItem += remain / colNum
        }
    }

}
