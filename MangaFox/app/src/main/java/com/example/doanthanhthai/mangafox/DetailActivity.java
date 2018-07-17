package com.example.doanthanhthai.mangafox;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.parser.AnimeDetailParser;
import com.example.doanthanhthai.mangafox.parser.AnimeParser;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.doanthanhthai.mangafox.HomeActivity.ANIME_ARG;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();
    private Anime mCurrentAnime;
    private ImageView thumbnailIv, coverIv;
    private TextView titleTv, yearTv, genresTv, durationTv, descriptionTv, toolbarTitleTv;
    private Button playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        thumbnailIv = findViewById(R.id.anime_thumbnail_iv);
        coverIv = findViewById(R.id.anime_cover_iv);
        playBtn = findViewById(R.id.play_btn);
        titleTv = findViewById(R.id.detail_anime_title);
        yearTv = findViewById(R.id.detail_anime_year_released);
        genresTv = findViewById(R.id.detail_anime_genres);
        durationTv = findViewById(R.id.detail_anime_duration);
        descriptionTv = findViewById(R.id.detail_anime_description);
        toolbarTitleTv = findViewById(R.id.order_toolbar_title);


        mCurrentAnime = (Anime) getIntent().getSerializableExtra(ANIME_ARG);
        if (mCurrentAnime == null) {
            Toast.makeText(DetailActivity.this, "[" + TAG + "] - " + "Don't have direct link!!!", Toast.LENGTH_SHORT).show();
        } else {
            toolbarTitleTv.setText(mCurrentAnime.title);
            new GetDetailAnimeTask().execute(mCurrentAnime.url);
        }
    }

    private class GetDetailAnimeTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetDetailAnimeTask.class.getSimpleName();

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            try {
                document = Jsoup.connect(strings[0]).timeout(3 * 1000).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Parse data fail: " + e.getMessage());
            }
            return document;
        }

        @Override
        protected void onPostExecute(final Document document) {
            if (document != null) {

                mCurrentAnime = AnimeDetailParser.getAnimeDetail(document, mCurrentAnime);
                Log.i(TAG, mCurrentAnime.title);

                Picasso.with(DetailActivity.this)
                        .load(mCurrentAnime.image)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(thumbnailIv);

                Picasso.with(DetailActivity.this)
                        .load(mCurrentAnime.coverImage)
                        .resize(750, 400)
                        .into(coverIv);

                titleTv.setText(mCurrentAnime.title);
                toolbarTitleTv.setText(mCurrentAnime.title);
                yearTv.setText(mCurrentAnime.year + "");
                genresTv.setText(mCurrentAnime.genres);
                durationTv.setText(mCurrentAnime.duration);
                descriptionTv.setText(mCurrentAnime.description);

            } else {
                Toast.makeText(DetailActivity.this, "Cannot get document web", Toast.LENGTH_LONG).show();

            }
            super.onPostExecute(document);
        }
    }
}
