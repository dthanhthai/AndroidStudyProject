package com.thaidt.demomvvm.ui.adapter

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter



class CustomBindingAdapter {

    @BindingAdapter("visibleGone")
    fun TextView.showHide(show: Boolean) {
        visibility = if (show) View.VISIBLE else View.GONE
    }
}