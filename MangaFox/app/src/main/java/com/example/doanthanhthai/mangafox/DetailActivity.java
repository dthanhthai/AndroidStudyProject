package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.parser.AnimeParser;
import com.example.doanthanhthai.mangafox.repository.AnimeRepository;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.ProgressAnimeView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DetailActivity.class.getSimpleName();
    private Anime mCurrentAnime;
    private ImageView thumbnailIv, coverIv;
    private TextView titleTv, yearTv, genresTv, durationTv, descriptionTv, toolbarTitleTv, otherTitleTv, newEpisodeTv;
    private Button playBtn, favoriteBtn;
    private ImageView backBtn;
    private LinearLayout otherTitleLayout, newEpisodeLayout;
    private ProgressAnimeView progressFullLayout, progressInfoLayout;
    private WebView webView;
    private AppWebViewClients webViewClient;
    private ProgressDialog progressDialog;

    private boolean isFavoriteAnime = false;
    private boolean isStartTransition = true;
    private GetDetailAnimeTask mGetDetailAnimeTask;

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
        progressFullLayout = findViewById(R.id.progress_full_screen_view);
        progressInfoLayout = findViewById(R.id.progress_info_view);
        backBtn = findViewById(R.id.toolbar_back_btn);
        favoriteBtn = findViewById(R.id.add_favorite_btn);
        webView = findViewById(R.id.webView);

        playBtn.setOnClickListener(this);
        favoriteBtn.setOnClickListener(this);
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

        progressFullLayout.setVisibility(View.VISIBLE);
        mCurrentAnime = AnimeDataManager.getInstance().getAnime();
        if (mCurrentAnime == null) {
            Toast.makeText(DetailActivity.this, "[" + TAG + "] - " + "Don't have direct link!!!", Toast.LENGTH_SHORT).show();
        } else {
            //Postpone the enter transition until image is loaded
            postponeEnterTransition();

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mGetDetailAnimeTask = new GetDetailAnimeTask();
                    mGetDetailAnimeTask.startTask(mCurrentAnime.getUrl());
                }
            });

            if (AnimeDataManager.getInstance().getBitmapDrawable() != null) {
                progressFullLayout.setVisibility(View.GONE);
                Glide.with(DetailActivity.this)
                        .load(AnimeDataManager.getInstance().getBitmapDrawable())
                        .thumbnail(0.2f)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                //Image successfully loaded into image view
                                scheduleStartPostponedTransition(thumbnailIv);
                                return false;
                            }
                        })
                        .into(thumbnailIv);
            } else {
                progressFullLayout.setVisibility(View.VISIBLE);
            }
            toolbarTitleTv.setText(mCurrentAnime.getTitle());

            //If anime is favorite, get data in cache favorite
            isFavoriteAnime = checkFavoriteAnime();
//            if (!isFavoriteAnime){
//            } else{
//                updateUIAnimeInfo();
//            }
        }
    }

    private boolean checkFavoriteAnime() {
        boolean isFavorite = false;
        List<Anime> favoriteAnimeList = AnimeDataManager.getInstance().getFavoriteAnimeList();
        for (int i = 0; i < favoriteAnimeList.size(); i++) {
            Anime anime = favoriteAnimeList.get(i);
            if (anime.getTitle().equalsIgnoreCase(mCurrentAnime.getTitle())) {
//                mCurrentAnime = anime;
//                AnimeDataManager.getInstance().setAnime(mCurrentAnime);
                AnimeDataManager.getInstance().setIndexFavoriteItem(i);
                isFavorite = true;
                break;
            }
        }
        setFavoriteUI(isFavorite);
        return isFavorite;
    }

    private void setFavoriteUI(boolean isFavorite) {
        Resources resources = this.getResources();
        if (!isFavorite) {
            favoriteBtn.setText(resources.getText(R.string.add_favorite));
            favoriteBtn.setBackground(resources.getDrawable(R.drawable.round_corner_border_add_favorite));
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_18dp, 0, 0, 0);
        } else {
            favoriteBtn.setText(resources.getText(R.string.remove_favorite));
            favoriteBtn.setBackground(resources.getDrawable(R.drawable.round_corner_border_remove_favorite));
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_back_btn:
                ActivityCompat.finishAfterTransition(this);
//                DetailActivity.this.finish();
                break;
            case R.id.play_btn:
                progressDialog.show();
                //If the first episode has direct link -> go to player activity
                //If the first episode doesn't have direct link -> perform get direct link then go to player activity
                Episode episode = mCurrentAnime.getEpisodeList().get(0);
                if (TextUtils.isEmpty(episode.getDirectUrl())) {
                    webViewClient.setRunGetSourceWeb(true);
                    webView.loadUrl(episode.getUrl());
                } else {
                    Intent intent = new Intent(DetailActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(HomeActivity.ANIME_ARG, mCurrentAnime);
                    startActivity(intent);
                    progressDialog.dismiss();
                }
                break;
            case R.id.add_favorite_btn:
                toggleFavoriteBtn();
                break;
        }
    }

    private void toggleFavoriteBtn() {
        if (isFavoriteAnime) {
            mCurrentAnime.setFavorite(false);
            boolean result = AnimeDataManager.getInstance().removeFavoriteAnime(AnimeDataManager.getInstance().getIndexFavoriteItem());
            if (result) {
                PreferenceHelper.getInstance(this).saveListFavoriteAnime(AnimeDataManager.getInstance().getFavoriteAnimeList());
            }
        } else {
            mCurrentAnime.setFavorite(true);
            boolean result = AnimeDataManager.getInstance().addFavoriteAnime(mCurrentAnime);
            if (result) {
                AnimeDataManager.getInstance().setIndexFavoriteItem(AnimeDataManager.getInstance().getFavoriteAnimeList().size() - 1);
                PreferenceHelper.getInstance(this).saveListFavoriteAnime(AnimeDataManager.getInstance().getFavoriteAnimeList());
            }
        }
        isFavoriteAnime = !isFavoriteAnime;
        setFavoriteUI(isFavoriteAnime);
    }

    private class GetDetailAnimeTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetDetailAnimeTask.class.getSimpleName();

        public void startTask(String url) {
            mGetDetailAnimeTask = new GetDetailAnimeTask();
            mGetDetailAnimeTask.execute(url);
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            try {
                document = Jsoup.connect(strings[0])
                        .timeout(Constant.INSTANCE.getTIME_OUT())
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
                Anime resultAnime = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getAnimeDetail(document, mCurrentAnime);

                if (resultAnime != null) {
                    mCurrentAnime = resultAnime;
                } else {
                    Log.e(TAG, "Cannot get CONTENT in document web");
                    Toast.makeText(DetailActivity.this, "Cannot get CONTENT in document web", Toast.LENGTH_LONG).show();
                    startTask(mCurrentAnime.getUrl());
                    return;
                }

                Log.i(TAG, mCurrentAnime.getTitle());

                updateUIAnimeInfo();
            } else {
                startPostponedEnterTransition();
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(DetailActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                startTask(mCurrentAnime.getUrl());
            }
        }
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        if (isStartTransition) {
            imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
            isStartTransition = false;
        }
    }

    private void updateUIAnimeInfo() {
        if (Utils.isValidContextForGlide(this)) {
            RequestOptions thumbRequestOptions = new RequestOptions();
            thumbRequestOptions.placeholder(R.drawable.placeholder);
            thumbRequestOptions.error(R.drawable.placeholder);
            Glide.with(DetailActivity.this)
                    .load(mCurrentAnime.getImage())
                    .thumbnail(0.2f)
                    .apply(thumbRequestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            //Image successfully loaded into image view
                            scheduleStartPostponedTransition(thumbnailIv);
                            return false;
                        }
                    })
                    .into(thumbnailIv);
        }

        if (Utils.isValidContextForGlide(this)) {
            RequestOptions coverRequestOptions = new RequestOptions();
            coverRequestOptions.placeholder(R.drawable.nature_cover);
            coverRequestOptions.error(R.drawable.nature_cover);
            Glide.with(DetailActivity.this)
                    .load(mCurrentAnime.getCoverImage())
                    .apply(coverRequestOptions)
                    .thumbnail(0.2f)
                    .into(coverIv);
        }

        if (!TextUtils.isEmpty(mCurrentAnime.getOrderTitle())) {
            otherTitleTv.setText(mCurrentAnime.getOrderTitle());
            otherTitleLayout.setVisibility(View.VISIBLE);
        } else {
            otherTitleLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mCurrentAnime.getNewEpisodeInfo())) {
            newEpisodeTv.setText(mCurrentAnime.getNewEpisodeInfo());
            newEpisodeLayout.setVisibility(View.VISIBLE);
        } else {
            newEpisodeLayout.setVisibility(View.GONE);
        }

        titleTv.setText(mCurrentAnime.getTitle());
        toolbarTitleTv.setText(mCurrentAnime.getTitle());
        yearTv.setText(mCurrentAnime.getYear() + "");
        genresTv.setText(mCurrentAnime.getGenres());
        durationTv.setText(mCurrentAnime.getDuration());
        descriptionTv.setText(mCurrentAnime.getDescription());

        playBtn.setVisibility(View.VISIBLE);
        favoriteBtn.setVisibility(View.VISIBLE);
        progressFullLayout.setVisibility(View.GONE);
        progressInfoLayout.setVisibility(View.GONE);
    }

    private class AppWebViewClients extends WebViewClient {
        boolean isRunGetSourceWeb = false;

        public AppWebViewClients() {

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

                        Anime result = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getDirectLinkDetail(playerDocument, webView, mCurrentAnime);
                        if (result != null) {
                            mCurrentAnime = result;

                        } else {
                            return true;
                        }
                        //If we have direct link -> go to player activity
                        if (!TextUtils.isEmpty(mCurrentAnime.getEpisodeList().get(0).getDirectUrl())) {

                            int indexFavoriteItem = AnimeDataManager.getInstance().getIndexFavoriteItem();
                            if (indexFavoriteItem > 0) {
                                List<Anime> favoriteAnimeList = AnimeDataManager.getInstance().getFavoriteAnimeList();
                                Anime favoriteAnime = favoriteAnimeList.get(indexFavoriteItem);
                                if (favoriteAnime.episodeList.size() == mCurrentAnime.episodeList.size()) {
                                    //Change current anime to cache favorite data
                                    mCurrentAnime = favoriteAnime;
//                                    AnimeDataManager.getInstance().setAnime(favoriteAnime);
                                } else {
                                    //Update episode data from cache favorite to current data then
                                    for (int i = 0; i < favoriteAnime.episodeList.size(); i++) {
                                        Episode ep = favoriteAnime.episodeList.get(i);
                                        if (!TextUtils.isEmpty(ep.getDirectUrl())) {
                                            mCurrentAnime.episodeList.set(i, ep);
                                        }
                                    }
                                    //Change cache favorite data to current anime data
                                    favoriteAnimeList.set(indexFavoriteItem, mCurrentAnime);
                                    //Save data
                                    PreferenceHelper.getInstance(DetailActivity.this)
                                            .saveListFavoriteAnime(favoriteAnimeList);
                                }
//                                favoriteAnimeList.set(indexFavoriteItem, mCurrentAnime);
//                                PreferenceHelper.getInstance(DetailActivity.this)
//                                        .saveListFavoriteAnime(favoriteAnimeList);
                                Log.i(TAG, "hello");
//                                AnimeDataManager.getInstance().setAnime(mCurrentAnime);
//                            }else{
                            }
                            AnimeDataManager.getInstance().setAnime(mCurrentAnime);
                            Intent intent = new Intent(DetailActivity.this, VideoPlayerActivity.class);
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
        AnimeDataManager.getInstance().resetIndexFavoriteItem();
        AnimeDataManager.getInstance().setBitmapDrawable(null);
        super.onDestroy();
    }


}
