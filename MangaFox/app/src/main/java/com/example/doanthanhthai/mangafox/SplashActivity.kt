package com.example.doanthanhthai.mangafox

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.doanthanhthai.mangafox.base.BaseActivity
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.repository.AnimeRepository
import com.example.doanthanhthai.mangafox.share.Constant
import com.example.doanthanhthai.mangafox.share.PreferenceHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.ArrayList

class SplashActivity : BaseActivity() {

    companion object {
        @JvmField
        val TAG: String = SplashActivity::class.java.simpleName!!
    }

    private var mGetAnimeHomePageTask: GetAnimeHomePageTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Make sure this is before calling super.onCreate
        if (PreferenceHelper.getInstance(this).getNightMode()) {
            setTheme(R.style.SplashThemeNight)
        } else {
            setTheme(R.style.SplashThemeDay)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preConfig(savedInstanceState)
        mapView()
        initData()
        Log.d(TAG, "onCreate")

        Handler().postDelayed(Runnable {
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
//            this@SplashActivity.startActivity(intent)
        }, 1000)
    }

    override fun mapView() {
        super.mapView()
    }

    override fun initData() {
        super.initData()

        //Load favorite anime
        val favoriteList = PreferenceHelper.getInstance(this).listFavoriteAnime
        AnimeDataManager.getInstance().favoriteAnimeList = favoriteList

        mGetAnimeHomePageTask = GetAnimeHomePageTask()
        mGetAnimeHomePageTask?.startTask(Constant.HOME_URL)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mGetAnimeHomePageTask != null) {
            mGetAnimeHomePageTask?.cancel(true)
            mGetAnimeHomePageTask = null
        }
    }

    private inner class GetAnimeHomePageTask : AsyncTask<String, Void, Document>() {
        private val TAG = GetAnimeHomePageTask::class.java.simpleName

        fun startTask(url: String) {
            mGetAnimeHomePageTask?.execute(url)
        }

        fun restartTask(url: String) {
            if (mGetAnimeHomePageTask != null) {
                mGetAnimeHomePageTask?.cancel(true)
                mGetAnimeHomePageTask = null
            }

            mGetAnimeHomePageTask = GetAnimeHomePageTask()
            mGetAnimeHomePageTask?.execute(url)
        }

        override fun doInBackground(vararg strings: String): Document? {
            var document: Document? = null
            var webCookies = PreferenceHelper.getInstance(this@SplashActivity).cookie
            try {
                val response = Jsoup.connect(strings[0])
                        .timeout(Constant.TIME_OUT)
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute()
                document = response.parse()

                webCookies = response.cookies()
                PreferenceHelper.getInstance(this@SplashActivity).saveCookie(webCookies)

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Parse data fail: " + e.message)
            }

            return document
        }

        override fun onPostExecute(document: Document?) {
            if (document != null) {
                var latestItems: List<Anime>? =
                        AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListAnimeItem(document)

                var bannerItems: List<Anime>? =
                        AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListBannerAnime(document)

                var totalPage = AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getPaginationAnime(document)!!
                //If latest page have more than 5 pages, hard code total is 5 pages
                if (totalPage > 5) {
                    totalPage = 5
                }

                if (latestItems == null || (latestItems != null && latestItems.isEmpty())
                        || bannerItems == null || (bannerItems != null && bannerItems.isEmpty())) {
                    Toast.makeText(this@SplashActivity, "Got confirm web, try again", Toast.LENGTH_SHORT).show()
                    mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (2 * 1000).toLong())
                }

                AnimeDataManager.getInstance().homeTotalPage = totalPage
                AnimeDataManager.getInstance().bannerList = bannerItems
                AnimeDataManager.getInstance().homeList = latestItems

                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                this@SplashActivity.startActivity(intent)
                this@SplashActivity.finish()

            } else {
                Log.e(TAG, "Cannot get DOCUMENT web")
                Toast.makeText(this@SplashActivity, "Cannot get DOCUMENT web", Toast.LENGTH_SHORT).show()
                mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (2 * 1000).toLong())
            }
            super.onPostExecute(document)
        }
    }
}
