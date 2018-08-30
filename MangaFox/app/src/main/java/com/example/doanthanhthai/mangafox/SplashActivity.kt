package com.example.doanthanhthai.mangafox

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.View
import com.example.doanthanhthai.mangafox.base.BaseActivity

class SplashActivity : BaseActivity() {

    companion object {
        @JvmField
        val TAG: String = SplashActivity::class.java.simpleName!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        if (savedInstanceState == null) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preConfig(savedInstanceState)
        mapView()
        initData()
        Log.d(TAG, "onCreate")

        Handler().postDelayed(Runnable {
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
            this@SplashActivity.startActivity(intent)
        }, 1000)
    }
}
