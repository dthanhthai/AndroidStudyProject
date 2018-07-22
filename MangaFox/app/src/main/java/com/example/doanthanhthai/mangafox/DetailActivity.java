package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.parser.AnimeDetailParser;
import com.example.doanthanhthai.mangafox.parser.AnimePlayerParser;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.example.doanthanhthai.mangafox.HomeActivity.ANIME_ARG;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DetailActivity.class.getSimpleName();
    private Anime mCurrentAnime;
    private ImageView thumbnailIv, coverIv;
    private TextView titleTv, yearTv, genresTv, durationTv, descriptionTv, toolbarTitleTv, otherTitleTv, newEpisodeTv;
    private Button playBtn;
    private ImageView backBtn;
    private LinearLayout otherTitleLayout, newEpisodeLayout;
    private FrameLayout progressBarLayout;
    private WebView webView;
    private AppWebViewClients webViewClient;
    private ProgressDialog progressDialog;

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
        toolbarTitleTv = findViewById(R.id.toolbar_title);
        otherTitleTv = findViewById(R.id.detail_anime_other_title);
        newEpisodeTv = findViewById(R.id.detail_anime_new_episode);
        otherTitleLayout = findViewById(R.id.detail_anime_other_title_layout);
        newEpisodeLayout = findViewById(R.id.detail_anime_new_episode_layout);
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        backBtn = findViewById(R.id.toolbar_back_btn);
        webView = findViewById(R.id.webView);

        playBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        progressBarLayout.setVisibility(View.VISIBLE);
//        mCurrentAnime = (Anime) getIntent().getSerializableExtra(ANIME_ARG);
        mCurrentAnime = AnimeDataManager.getInstance().getAnime();
        if (mCurrentAnime == null) {
            Toast.makeText(DetailActivity.this, "[" + TAG + "] - " + "Don't have direct link!!!", Toast.LENGTH_SHORT).show();
        } else {
            toolbarTitleTv.setText(mCurrentAnime.title);
            new GetDetailAnimeTask().execute(mCurrentAnime.url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back_btn:
                DetailActivity.this.finish();
                break;
            case R.id.play_btn:
                progressDialog.show();
                //If the first episode has direct link -> go to player activity
                //If the first episode doesn't have direct link -> perform get direct link then go to player activity
                Episode episode = mCurrentAnime.episodeList.get(0);
                if (TextUtils.isEmpty(episode.directUrl)) {
                    webViewClient.setRunGetSourceWeb(true);
                    webView.loadUrl(episode.url);
                } else {
                    Intent intent = new Intent(DetailActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(HomeActivity.ANIME_ARG, mCurrentAnime);
                    startActivity(intent);
                    progressDialog.dismiss();
                }
                break;
        }
    }

    private class GetDetailAnimeTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetDetailAnimeTask.class.getSimpleName();

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            try {
                document = Jsoup.connect(strings[0])
                        .timeout(Constant.TIME_OUT)
                        .userAgent(Constant.USER_AGENT)
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Parse data fail: " + e.getMessage());
            }
            return document;
        }

        @Override
        protected void onPostExecute(final Document document) {
            super.onPostExecute(document);
            if (document != null) {

                Anime resultAnime = AnimeDetailParser.getAnimeDetail(document, mCurrentAnime);

                if(resultAnime != null){
                    mCurrentAnime = resultAnime;
                }else{
                    Log.e(TAG, "Cannot get CONTENT in document web");
                    Toast.makeText(DetailActivity.this, "Cannot get CONTENT in document web", Toast.LENGTH_LONG).show();
                    GetDetailAnimeTask.this.execute(mCurrentAnime.url);
                    return;
                }

                AnimeDataManager.getInstance().setAnime(mCurrentAnime);

                Log.i(TAG, mCurrentAnime.title);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.placeholder);

                Glide.with(DetailActivity.this)
                        .load(mCurrentAnime.image)
                        .thumbnail(0.4f)
                        .apply(requestOptions)
                        .into(thumbnailIv);

                Glide.with(DetailActivity.this)
                        .load(mCurrentAnime.coverImage)
                        .thumbnail(0.2f)
                        .into(coverIv);

//                Picasso.with(DetailActivity.this)
//                        .load(mCurrentAnime.image)
//                        .error(R.drawable.placeholder)
//                        .placeholder(R.drawable.placeholder)
//                        .into(thumbnailIv);
//
//                Picasso.with(DetailActivity.this)
//                        .load(mCurrentAnime.coverImage)
//                        .resize(750, 400)
//                        .into(coverIv);

                if (!TextUtils.isEmpty(mCurrentAnime.orderTitle)) {
                    otherTitleTv.setText(mCurrentAnime.orderTitle);
                    otherTitleLayout.setVisibility(View.VISIBLE);
                } else {
                    otherTitleLayout.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(mCurrentAnime.newEpisodeInfo)) {
                    newEpisodeTv.setText(mCurrentAnime.newEpisodeInfo);
                    newEpisodeLayout.setVisibility(View.VISIBLE);
                } else {
                    newEpisodeLayout.setVisibility(View.GONE);
                }

                titleTv.setText(mCurrentAnime.title);
                toolbarTitleTv.setText(mCurrentAnime.title);
                yearTv.setText(mCurrentAnime.year + "");
                genresTv.setText(mCurrentAnime.genres);
                durationTv.setText(mCurrentAnime.duration);
                descriptionTv.setText(mCurrentAnime.description);

                progressBarLayout.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(DetailActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                GetDetailAnimeTask.this.execute(mCurrentAnime.url);
            }
        }
    }

    private class AppWebViewClients extends WebViewClient {
        boolean isRunGetSourceWeb = false;

        public AppWebViewClients() {
//            progress.setVisibility(View.VISIBLE);
        }

        public void setRunGetSourceWeb(boolean runGetSourceWeb) {
            isRunGetSourceWeb = runGetSourceWeb;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("source://")) {
                try {
                    String html = URLDecoder.decode(url, "UTF-8").substring(9);

                    Document playerDocument = Jsoup.parse(html);
                    if (playerDocument != null) {

                        Anime reuslt = AnimePlayerParser.getDirectLinkDetail(playerDocument, webView, mCurrentAnime);
                        if (reuslt != null) {
                            mCurrentAnime = reuslt;
                            AnimeDataManager.getInstance().setAnime(mCurrentAnime);
                        } else {
                            return true;
                        }
                        //If we have direct link -> go to player activity
                        if (!TextUtils.isEmpty(mCurrentAnime.episodeList.get(0).directUrl)) {
                            Intent intent = new Intent(DetailActivity.this, VideoPlayerActivity.class);
//                            intent.putExtra(HomeActivity.ANIME_ARG, mCurrentAnime);
                            startActivity(intent);
                            webView.stopLoading();
                            progressDialog.dismiss();
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                    Toast.makeText(DetailActivity.this, "[" + TAG + "] - " + "Can not get link episode", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (isRunGetSourceWeb) {
                webView.loadUrl(
                        "javascript:changeHtml5();" +
                                "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                isRunGetSourceWeb = false;
            }
        }

    }


    @Override
    protected void onDestroy() {
        AnimeDataManager.getInstance().setAnime(null);
        super.onDestroy();
    }
}
