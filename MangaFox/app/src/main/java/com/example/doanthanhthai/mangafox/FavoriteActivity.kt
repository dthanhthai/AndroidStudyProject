package com.example.doanthanhthai.mangafox

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.Toast
import com.example.doanthanhthai.mangafox.adapter.ResultAnimeAdapter
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager
import com.example.doanthanhthai.mangafox.model.Anime
import com.example.doanthanhthai.mangafox.share.Utils
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager
import kotlinx.android.synthetic.main.activity_favorite.*;

class FavoriteActivity : AppCompatActivity(), View.OnClickListener, ResultAnimeAdapter.OnResultAnimeAdapterListener {

    val TAG: String = FavoriteActivity::class.java.simpleName!!
    var gridLayoutManager: GridLayoutManager? = null
    var mResultAnimeAdapter: ResultAnimeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        supportActionBar?.hide()

        toolbar_back_btn.setOnClickListener(this)

        gridLayoutManager = AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150))
        mResultAnimeAdapter = ResultAnimeAdapter(this)
        result_anime_rv.layoutManager = gridLayoutManager
        result_anime_rv.adapter = mResultAnimeAdapter

    }

    override fun onResume() {
        super.onResume()
        if (AnimeDataManager.getInstance().favoriteAnimeList != null) {
            if (AnimeDataManager.getInstance().favoriteAnimeList.isEmpty()) {
                empty_result_tv.visibility = View.VISIBLE
            } else {
                empty_result_tv.visibility = View.GONE
                mResultAnimeAdapter?.setAnimeList(AnimeDataManager.getInstance().favoriteAnimeList)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.toolbar_back_btn -> this.finish();
        }
    }

    override fun onItemClick(item: Anime?, position: Int) {
        AnimeDataManager.getInstance().anime = item
        val intent = Intent(this, DetailActivity::class.java)
        startActivity(intent)
        Toast.makeText(this, item?.title, Toast.LENGTH_SHORT).show()
    }
}
