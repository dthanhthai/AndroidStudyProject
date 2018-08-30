package com.example.doanthanhthai.mangafox.base

import android.os.Bundle
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.example.doanthanhthai.mangafox.R

/**
 * Created by thaidt1 on 8/13/2018.
 */
open abstract class BaseActivity : AppCompatActivity() {

    var mToolbar: Toolbar? = null
    @JvmField
    var mTaskHandler: Handler? = null

    open fun preConfig(savedInstanceState: Bundle?) {
//        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        setupActionBar()
    }

    open fun mapView() {

    }

    open fun initData() {
        mTaskHandler = Handler()
    }

    private fun setupActionBar() {
        findViewById<View>(R.id.toolbar_layout)?.let {
            mToolbar = it as Toolbar
            setSupportActionBar(mToolbar)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTaskHandler?.let {
            it.removeCallbacksAndMessages(null)
        }
        mTaskHandler = null
    }
}