package com.example.doanthanhthai.mangafox.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.example.doanthanhthai.mangafox.R
import kotlinx.android.synthetic.main.progress_anime_view.view.*

/**
 * Created by ThaiDT1 on 7/23/2018.
 */
class ProgressAnimeView : FrameLayout {

    constructor(context: Context?) : super(context) {
        createUI()
    }

    constructor(context: Context?,
                attrs: AttributeSet?) : super(context, attrs) {
        createUI()
    }

    constructor(context: Context?,
                attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        createUI()
    }

    constructor(context: Context?,
                attrs: AttributeSet?,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        createUI()
    }

    fun createUI() {
        LayoutInflater.from(context).inflate(R.layout.progress_anime_view, this, true)

        Glide.with(context!!).load(R.raw.round_loading).into(progress_iv)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}