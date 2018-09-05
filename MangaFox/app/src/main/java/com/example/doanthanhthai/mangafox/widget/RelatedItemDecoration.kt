package com.example.doanthanhthai.mangafox.widget

import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.doanthanhthai.mangafox.share.Utils

/**
 * Created by ThaiDT1 on 9/5/2018.
 */
class RelatedItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        val childCount = parent?.getChildCount()
        for (i in 0 until childCount!!) {
            if (parent.getChildAdapterPosition(view) != (parent.adapter.itemCount - 1)) {
                outRect?.right = Utils.convertDpToPixel(view?.context, 5)
            }
        }
    }

    override fun onDraw(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.onDraw(c, parent, state)
    }
}