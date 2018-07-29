package com.example.doanthanhthai.mangafox

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.example.doanthanhthai.mangafox.adapter.FavoriteAnimeAdapter
import com.example.doanthanhthai.mangafox.adapter.ResultAnimeAdapter
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.model.Episode
import com.example.doanthanhthai.mangafox.share.DynamicColumnHelper
import kotlinx.android.synthetic.main.activity_favorite.*;

class FavoriteActivity : AppCompatActivity(), View.OnClickListener, FavoriteAnimeAdapter.OnFavoriteAnimeAdapterListener {

    val TAG: String = FavoriteActivity::class.java.simpleName!!
    var gridLayoutManager: GridLayoutManager? = null
    var mFavoriteAnimeAdapter: FavoriteAnimeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
//        supportActionBar?.hide()
        setupActionBar()

        toolbar_back_btn.setOnClickListener(this)

        mFavoriteAnimeAdapter = FavoriteAnimeAdapter(this)
        val columnHelper = DynamicColumnHelper(this)

        mFavoriteAnimeAdapter?.setDynamicColumnHelper(columnHelper)
        gridLayoutManager = GridLayoutManager(this, columnHelper.colNum, RecyclerView.VERTICAL, false)
        result_anime_rv.layoutManager = gridLayoutManager
        result_anime_rv.adapter = mFavoriteAnimeAdapter

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_layout)
    }

    override fun onResume() {
        super.onResume()
        if (AnimeDataManager.getInstance().favoriteAnimeList != null) {
            if (AnimeDataManager.getInstance().favoriteAnimeList.isEmpty()) {
                empty_result_tv.visibility = View.VISIBLE
            } else {
                empty_result_tv.visibility = View.GONE
//                val tmpList: MutableList<Anime> = mutableListOf<Anime>()
//                AnimeDataManager.getInstance().favoriteAnimeList.forEach { tmpList.add(it) }
                mFavoriteAnimeAdapter?.setAnimeList(AnimeDataManager.getInstance().favoriteAnimeList)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.toolbar_back_btn -> this.finish();
        }
    }

    override fun onItemClick(item: Anime?, position: Int) {
        var tmpAnime: Anime? = Anime()
        var tmpEpList: MutableList<Episode> = mutableListOf<Episode>()
        tmpAnime?.title = item?.title
        tmpAnime?.url = item?.url
        tmpAnime?.orderTitle = item?.orderTitle
        tmpAnime?.image = item?.image
        tmpAnime?.bannerImage = item?.bannerImage
        tmpAnime?.coverImage = item?.coverImage
        tmpAnime?.episodeInfo = item?.episodeInfo
        tmpAnime?.rate = item?.rate
        tmpAnime?.year = item?.year!!
        tmpAnime?.description = item?.description
        tmpAnime?.genres = item?.genres
        tmpAnime?.duration = item?.duration
        tmpAnime?.newEpisodeInfo = item?.newEpisodeInfo
        tmpAnime?.isFavorite = item?.isFavorite
        tmpAnime?.isFavorite = item?.isFavorite
        item?.episodeList.forEach {
            var tmpEp: Episode = Episode()
            tmpEp.name = it.name
            tmpEp.url = it.url
            tmpEp.directUrl = it.directUrl
            tmpEp.fullName = it.fullName
            tmpEpList.add(tmpEp)
        }
        tmpAnime?.episodeList = tmpEpList
        AnimeDataManager.getInstance().anime = tmpAnime

        val viewHolder = result_anime_rv.findViewHolderForPosition(position) as FavoriteAnimeAdapter.FavoriteAnimeViewHolder
        val imagePair = Pair
                .create(viewHolder.posterImg as View, getString(R.string.transition_image))
        val titlePair = Pair
                .create(viewHolder.animeTitleTv as View, getString(R.string.transition_title))
        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imagePair, titlePair)
        val intent = Intent(this, DetailActivity::class.java)
        AnimeDataManager.getInstance().thumbnailBitmap = (viewHolder.posterImg.drawable as BitmapDrawable).bitmap
        startActivity(intent, options.toBundle())
        Toast.makeText(this, item?.title, Toast.LENGTH_SHORT).show()
    }
}
