package com.example.doanthanhthai.mangafox.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.util.Pair
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.doanthanhthai.mangafox.DetailActivity
import com.example.doanthanhthai.mangafox.R
import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.model.Category
import com.example.doanthanhthai.mangafox.repository.AnimeRepository
import com.example.doanthanhthai.mangafox.share.Constant
import com.example.doanthanhthai.mangafox.share.DynamicColumnHelper
import com.example.doanthanhthai.mangafox.share.PreferenceHelper
import com.example.doanthanhthai.mangafox.share.Utils
import kotlinx.android.synthetic.main.fragment_anime_genre.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL

/**
 * Created by ThaiDT1 on 8/20/2018.
 */
class AnimeYearFragment : Fragment(), LatestEpisodeAdapter.OnLatestEpisodeAdapterListener, View.OnClickListener, NestedScrollView.OnScrollChangeListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toolbar_back_btn -> {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            R.id.menu_btn -> {
                showFilterSortDialog()
            }
        }
    }

    companion object {
        @JvmField
        val TAG = AnimeYearFragment::class.java.simpleName
    }

    private var mGetAnimeGenreTask: GetAnimeGenreTask? = null
    private var mGetAnimeTask: GetAnimeTask? = null
    private var mGetAnimeByPageNumTask: GetAnimeByPageNumTask? = null
    private var mLatestEpisodeAdapter: LatestEpisodeAdapter? = null
    private var mGridLayoutManager: GridLayoutManager? = null
    private var mTaskHandler: Handler? = null
    private var mTotalPage: Int = 1
    private var mCurrentPage: Int = 1
    private var currentUrl: String = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_anime_genre, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress_full_screen_view.visibility = View.VISIBLE
        toolbar_back_btn.setOnClickListener(this)
        menu_btn.setOnClickListener(this)
        nested_scroll_view.setOnScrollChangeListener(this)

        mLatestEpisodeAdapter = LatestEpisodeAdapter(this)
        val dynamicColumnHelper = DynamicColumnHelper(activity!!)
        list_anime_rv.isNestedScrollingEnabled = false
        mLatestEpisodeAdapter?.setDynamicColumnHelper(dynamicColumnHelper)
        mGridLayoutManager = GridLayoutManager(activity, dynamicColumnHelper.colNum, RecyclerView.VERTICAL, false)
        list_anime_rv.layoutManager = mGridLayoutManager
        list_anime_rv.adapter = mLatestEpisodeAdapter
//        setRecyclerViewScrollListener()

        mGetAnimeGenreTask = GetAnimeGenreTask()
        mGetAnimeTask = GetAnimeTask()
        mGetAnimeByPageNumTask = GetAnimeByPageNumTask()
        mTaskHandler = Handler()
        mGetAnimeGenreTask?.startTask("http://animehay.tv/")
    }

    override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (v?.getChildAt(v.getChildCount() - 1) != null) {
            if (scrollY >= v.getChildAt(v.getChildCount() - 1).measuredHeight - v.measuredHeight && scrollY > oldScrollY) {
                mGridLayoutManager?.let {

                    val visibleItemCount: Int = it.getChildCount()
                    val totalItemCount: Int = it.getItemCount()
                    val pastVisiblesItems: Int = it.findFirstVisibleItemPosition()
                    if (mCurrentPage < mTotalPage) {

                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            mGetAnimeByPageNumTask?.startTask(currentUrl + Constant.PAGE_PARAM + ++mCurrentPage)
                            Log.i(TAG, "Load more")
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(item: Anime?, position: Int) {
        AnimeDataManager.getInstance().anime = item

        val viewHolder = list_anime_rv.findViewHolderForPosition(position) as LatestEpisodeAdapter.LatestViewHolder
        val imagePair = Pair
                .create(viewHolder.posterImg as View, getString(R.string.transition_image))
        val titlePair = Pair
                .create(viewHolder.animeTitleTv as View, getString(R.string.transition_title))
        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity!!, imagePair, titlePair)
        if (viewHolder.posterImg.drawable is BitmapDrawable) {
            AnimeDataManager.getInstance().thumbnailBitmap = (viewHolder.posterImg.drawable as BitmapDrawable).bitmap
        } else {
            AnimeDataManager.getInstance().thumbnailBitmap = null
        }
        val intent = Intent(activity, DetailActivity::class.java)
        startActivity(intent, options.toBundle())
        Toast.makeText(activity!!, item?.title, Toast.LENGTH_SHORT).show()
    }

    private var previousSelectedSort: Int = 0
    private var genreItemsArray: Array<String?>? = null
    var genreItems: List<Category>? = ArrayList()

    private fun showFilterSortDialog() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("List Genres")
        if (genreItemsArray != null && genreItemsArray!!.isNotEmpty()) {
            builder.setSingleChoiceItems(genreItemsArray, previousSelectedSort, DialogInterface.OnClickListener { dialog, which ->
                val orderByTxt = genreItemsArray!![which]
                if (!Utils.isTextEmpty(orderByTxt)) {
                    previousSelectedSort = which
                    toolbar_title.text = genreItems?.get(which)?.name
                    currentUrl = genreItems?.get(which)?.url!!
                    progress_full_screen_view.visibility = View.VISIBLE
                    mGetAnimeTask?.restartTask(currentUrl)
                } else {
                    Toast.makeText(activity, "Error filter", Toast.LENGTH_SHORT).show()
                }

                //Scroll to top
                nested_scroll_view.scrollTo(0, 0)

                dialog.dismiss()
            })

            builder.setNegativeButton("Close") { dialog, which -> dialog.dismiss() }
            builder.show()
        } else {
            Toast.makeText(activity, "Error filter", Toast.LENGTH_SHORT).show()
        }
    }


    private inner class GetAnimeGenreTask : AsyncTask<String, Void, Document>() {
        private val TAG = GetAnimeGenreTask::class.java.simpleName

        fun startTask(url: String) {
            mGetAnimeGenreTask?.execute(url)
        }

        fun restartTask(url: String) {
            if (mGetAnimeGenreTask != null) {
                mGetAnimeGenreTask?.cancel(true)
                mGetAnimeGenreTask = null
            }

            mGetAnimeGenreTask = GetAnimeGenreTask()
            mGetAnimeGenreTask?.execute(url)
        }

        override fun doInBackground(vararg strings: String): Document? {
            var document: Document? = null
            var webCookies = PreferenceHelper.getInstance(activity).cookie
            try {
                val response = Jsoup.connect(strings[0])
                        .timeout(Constant.TIME_OUT)
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute()
                document = response.parse()

                webCookies = response.cookies()
                PreferenceHelper.getInstance(activity).saveCookie(webCookies)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Parse data fail: " + e.message)
            }

            return document
        }

        override fun onPostExecute(document: Document?) {
            if (document != null) {

                genreItems = AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListYearGenre(document)

                if (genreItems != null && genreItems?.size!! > 0) {
//                    mLatestEpisodeAdapter.setAnimeList(genreItems)
                    toolbar_title.text = genreItems?.get(0)?.name
                    currentUrl = genreItems?.get(0)?.url!!
                    mGetAnimeTask?.startTask(currentUrl)

                    genreItemsArray = arrayOfNulls<String>(genreItems!!.size)
                    for (i in genreItems!!.indices) {
                        genreItemsArray!![i] = genreItems!!.get(i).name
                    }
                } else {
                    Toast.makeText(activity, "Got confirm web, try again", Toast.LENGTH_SHORT).show()
                    mTaskHandler!!.postDelayed(Runnable { restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
                }

//                progress_full_screen_view.visibility = View.GONE
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web")
                Toast.makeText(activity, "Cannot get DOCUMENT web", Toast.LENGTH_SHORT).show()
                mTaskHandler!!.postDelayed(Runnable { restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
            }
            super.onPostExecute(document)
        }
    }

    private inner class GetAnimeTask : AsyncTask<String, Void, Document>() {
        private val TAG = GetAnimeTask::class.java.simpleName

        fun startTask(url: String) {
            mGetAnimeTask?.execute(url)
        }

        fun restartTask(url: String) {
            if (mGetAnimeTask != null) {
                mGetAnimeTask?.cancel(true)
                mGetAnimeTask = null
            }

            mGetAnimeTask = GetAnimeTask()
            mGetAnimeTask?.execute(url)
        }

        override fun doInBackground(vararg strings: String): Document? {
            var document: Document? = null
            var webCookies = PreferenceHelper.getInstance(activity).cookie
            try {
                var urlText = strings[0]
                try {
                    val url = URL(strings[0])
                } catch (e: Exception) {
                    urlText = "http://animehay.tv" + strings[0]
                }
                val response = Jsoup.connect(urlText)
                        .timeout(Constant.TIME_OUT)
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute()
                document = response.parse()

                webCookies = response.cookies()
                PreferenceHelper.getInstance(activity).saveCookie(webCookies)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Parse data fail: " + e.message)
            }

            return document
        }

        override fun onPostExecute(document: Document?) {
            if (document != null) {

                var animeList: List<Anime>? = AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListAnimeItem(document)

                mTotalPage = AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getPaginationAnime(document)!!
                mCurrentPage = 1

                if (animeList != null && !animeList.isEmpty()) {
                    mLatestEpisodeAdapter?.setAnimeList(animeList)

                } else {
                    Toast.makeText(activity, "Got confirm web, try again", Toast.LENGTH_SHORT).show()
                    mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
                }

                progress_full_screen_view.visibility = View.GONE
                //Margin toolbar_height size to see toolbar when progress view is shown again
                val layoutParam: RelativeLayout.LayoutParams = progress_full_screen_view.layoutParams as RelativeLayout.LayoutParams
                layoutParam.topMargin = activity!!.resources.getDimension(R.dimen.toolbar_height).toInt();
                progress_full_screen_view.layoutParams = layoutParam
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web")
                Toast.makeText(activity, "Cannot get DOCUMENT web", Toast.LENGTH_SHORT).show()
                mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
            }
            super.onPostExecute(document)
        }
    }

    private inner class GetAnimeByPageNumTask : AsyncTask<String, Void, Document>() {
        private val TAG = GetAnimeTask::class.java.simpleName

        fun startTask(url: String) {
            if (mGetAnimeByPageNumTask != null) {
                mGetAnimeByPageNumTask?.cancel(true)
                mGetAnimeByPageNumTask = null
            }

            mGetAnimeByPageNumTask = GetAnimeByPageNumTask()
            mGetAnimeByPageNumTask?.execute(url)
        }

        fun restartTask(url: String) {
            mGetAnimeByPageNumTask = GetAnimeByPageNumTask()
            mGetAnimeByPageNumTask?.execute(url)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress_load_more_layout.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg strings: String): Document? {
            var document: Document? = null
            var webCookies = PreferenceHelper.getInstance(activity).cookie
            try {
                val response = Jsoup.connect(strings[0])
                        .timeout(Constant.TIME_OUT)
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute()
                document = response.parse()

                webCookies = response.cookies()
                PreferenceHelper.getInstance(activity).saveCookie(webCookies)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Parse data fail: " + e.message)
            }

            return document
        }

        override fun onPostExecute(document: Document?) {
            if (document != null) {
                var moreItems: List<Anime>? = AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListAnimeItem(document)

                if (moreItems != null && !moreItems.isEmpty()) {
                    mLatestEpisodeAdapter?.addMoreAnime(moreItems)
                } else {
                    mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
                }

                progress_load_more_layout.visibility = View.GONE
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web")
                Toast.makeText(activity, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show()
                mTaskHandler!!.postDelayed({ restartTask(Constant.HOME_URL) }, (3 * 1000).toLong())
            }
            super.onPostExecute(document)
        }
    }
}