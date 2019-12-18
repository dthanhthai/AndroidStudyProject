package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.doanthanhthai.mangafox.adapter.RelatedContentAdapter;
import com.example.doanthanhthai.mangafox.base.BaseActivity;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.model.NavigationModel;
import com.example.doanthanhthai.mangafox.model.RelatedContent;
import com.example.doanthanhthai.mangafox.repository.AnimeRepository;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.ProgressAnimeView;
import com.example.doanthanhthai.mangafox.widget.RelatedItemDecoration;
import com.example.doanthanhthai.mangafox.widget.StartSnapHelper;
import com.google.android.gms.cast.framework.CastButtonFactory;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DetailActivity extends BaseActivity implements View.OnClickListener, RelatedContentAdapter.RelatedAdapterListener {
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
    private RecyclerView relatedContentRv;

    private boolean isFavoriteAnime = false;
    private boolean isStartTransition = true;
    private GetDetailAnimeTask mGetDetailAnimeTask;
    private Toolbar mToolbar;
    private MenuItem mediaRouteMenuItem;
    private Handler adTimerHandler;
    private Timer timer = null;
    private boolean adReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        preConfig(savedInstanceState);
        mapView();
        initData();
        Log.d(TAG, "onCreate");
    }

    @Override
    public void preConfig(Bundle savedInstanceState) {
        super.preConfig(savedInstanceState);
    }

    @Override
    public void mapView() {
        super.mapView();
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
        relatedContentRv = findViewById(R.id.related_rv);
    }

    @Override
    public void initData() {
        super.initData();
        startTimer();
        playBtn.setOnClickListener(this);
        favoriteBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(true);
        webView.getSettings().setLoadsImagesAutomatically(false);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        mTaskHandler = new Handler();

        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        progressInfoLayout.setVisibility(View.VISIBLE);
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

            if (AnimeDataManager.getInstance().getThumbnailBitmap() != null) {
                progressFullLayout.setVisibility(View.GONE);
                Glide.with(DetailActivity.this)
                        .load(AnimeDataManager.getInstance().getThumbnailBitmap())
                        .apply(new RequestOptions().override(Utils.convertDpToPixel(DetailActivity.this, 60), Utils.convertDpToPixel(DetailActivity.this, 75)))
                        .thumbnail(0.2f)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                adReceived = true;
                                thumbnailIv.setBackgroundColor(DetailActivity.this.getResources().getColor(R.color.slight_gray));
                                scheduleStartPostponedTransition(thumbnailIv);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                //Image successfully loaded into image view
                                adReceived = true;
                                scheduleStartPostponedTransition(thumbnailIv);
                                return false;
                            }
                        })
                        .into(thumbnailIv);
            } else {
                adReceived = true;
                thumbnailIv.setBackgroundColor(this.getResources().getColor(R.color.slight_gray));
                scheduleStartPostponedTransition(thumbnailIv);
            }
            toolbarTitleTv.setText(mCurrentAnime.getTitle());

            //If anime is favorite, get data in cache favorite
            isFavoriteAnime = checkFavoriteAnime();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
        }
        return true;
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        setSupportActionBar(mToolbar);
    }

    private boolean checkFavoriteAnime() {
        boolean isFavorite = false;
        List<Anime> favoriteAnimeList = AnimeDataManager.getInstance().getFavoriteAnimeList();
        if (favoriteAnimeList != null) {
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
        }
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
                    startVideoPlayerActivity();
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

    @Override
    public void onItemClicked(@NotNull RelatedContent item) {
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
        progressFullLayout.setVisibility(View.VISIBLE);
        mCurrentAnime = new Anime();
        mGetDetailAnimeTask.startTask(item.getUrl());
//            }
//        });
    }

    private class GetDetailAnimeTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetDetailAnimeTask.class.getSimpleName();

        public void startTask(String url) {
            if (mGetDetailAnimeTask != null) {
                mGetDetailAnimeTask.cancel(true);
                mGetDetailAnimeTask = null;
            }

            mGetDetailAnimeTask = new GetDetailAnimeTask();
            mGetDetailAnimeTask.execute(url);
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            Map<String, String> webCookies = PreferenceHelper.getInstance(DetailActivity.this).getCookie();
            try {
                Connection.Response response = Jsoup.connect(strings[0])
                        .timeout(Constant.INSTANCE.getTIME_OUT())
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute();
                document = response.parse();

                webCookies = response.cookies();
                PreferenceHelper.getInstance(DetailActivity.this).saveCookie(webCookies);
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
                    mTaskHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startTask(mCurrentAnime.getUrl());
                        }
                    }, 3 * 1000);
                    return;
                }

                Log.i(TAG, mCurrentAnime.getTitle());

                updateUIAnimeInfo();
            } else {
                startPostponedEnterTransition();
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(DetailActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                mTaskHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTask(mCurrentAnime.getUrl());
                    }
                }, 3 * 1000);
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
        if (mCurrentAnime.getRelatedContents() != null && !mCurrentAnime.getRelatedContents().isEmpty()) {
            RelatedContentAdapter relatedContentAdapter = new RelatedContentAdapter(mCurrentAnime.getRelatedContents(), this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            relatedContentRv.setLayoutManager(linearLayoutManager);
            relatedContentRv.setAdapter(relatedContentAdapter);
            for (int i = 0; i < mCurrentAnime.getRelatedContents().size(); i++) {
                if (mCurrentAnime.getRelatedContents().get(i).isCurrent()) {
                    relatedContentRv.scrollToPosition(i);
                    break;
                }
            }
            relatedContentRv.setOnFlingListener(null);
            StartSnapHelper startSnapHelper = new StartSnapHelper();
            startSnapHelper.attachToRecyclerView(relatedContentRv);

            relatedContentRv.addItemDecoration(new RelatedItemDecoration());
        } else {
            relatedContentRv.setVisibility(View.GONE);
        }

        if (Utils.isValidContextForGlide(this) && AnimeDataManager.getInstance().getThumbnailBitmap() == null) {
            RequestOptions thumbRequestOptions = new RequestOptions();
            thumbRequestOptions.placeholder(R.color.slight_gray);
            thumbRequestOptions.error(R.color.slight_gray);
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
//                            scheduleStartPostponedTransition(thumbnailIv);
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
                    .thumbnail(0.2f)
                    .apply(coverRequestOptions)
                    .into(coverIv);

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

            //Margin toolbar_height size to see toolbar when progress view is shown again
            RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) progressFullLayout.getLayoutParams();
            layoutParam.topMargin = (int) this.getResources().getDimension(R.dimen.toolbar_height);
            progressFullLayout.setLayoutParams(layoutParam);

            AnimeDataManager.getInstance().resetThumbnailBitmap();
        }
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
                            startVideoPlayerActivity();
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
//                webView.loadUrl(
//                        "javascript:changeHtml5();" +
//                                "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                webView.loadUrl(
                        "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                isRunGetSourceWeb = false;
            }
        }

    }

    private void startVideoPlayerActivity() {
        Intent intent = new Intent(DetailActivity.this, VideoPlayerActivity.class);
        AnimeDataManager.getInstance().setCoverBitmap(((BitmapDrawable) coverIv.getDrawable()).getBitmap());
        startActivity(intent);
        webView.stopLoading();
        progressDialog.dismiss();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null; // remove old timer
        }
    }

    public void startTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null; // remove old timer
        }
        timer = new Timer();
        adTimerHandler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                adTimerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopTimer();
                        if (!adReceived) { // 1 sec and thumb image isn't loaded successfully
                            thumbnailIv.setBackgroundColor(DetailActivity.this.getResources().getColor(R.color.slight_gray));
                            scheduleStartPostponedTransition(thumbnailIv);
                        }
                    }
                });
            }
        }, 300);

        if (adReceived) {
            thumbnailIv.setBackgroundColor(this.getResources().getColor(R.color.slight_gray));
            scheduleStartPostponedTransition(thumbnailIv);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        AnimeDataManager.getInstance().setAnime(null);
        AnimeDataManager.getInstance().resetIndexFavoriteItem();
        AnimeDataManager.getInstance().resetThumbnailBitmap();
        AnimeDataManager.getInstance().resetCoverBitmap();
        super.onDestroy();
    }

}
