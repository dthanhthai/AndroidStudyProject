package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter;
import com.example.doanthanhthai.mangafox.adapter.SlideBannerAdapter;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.parser.AnimeParser;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeActivity extends AppCompatActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener,
        View.OnClickListener, SlideBannerAdapter.OnSlideBannerAdapterListener, NestedScrollView.OnScrollChangeListener {

    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String ANIME_ARG = "animeArg";
    public static final String KEYWORD_ARG = "keywordArg";

    private WebView webView;
    private WebView confirmWebView;
    private AppWebViewClients webViewClient;
    private ImageView searchIconIv;
    private ImageView mangaIconIv;
    private RecyclerView latestEpisodeRV;
    private LatestEpisodeAdapter mLatestEpisodeAdapter;
    private NestedScrollView nestedScrollView;
    private ViewPager mSlideViewPager;
    private CircleIndicator mSlideIndicator;
    private static int mSlideCurrentPage = 0;
    private int mTotalPage = 1;
    private int mCurrentPage = 1;
    private ProgressDialog progressDialog;
    private boolean isAutoChangeBanner = false;
    private GetAnimeHomePageTask mGetAnimeHomePageTask;
    private GetAnimeByPageNumTask mGetAnimeByPageNumTask;
    private GridLayoutManager mGridLayoutManager;
    private FrameLayout progressBarLayout;
    private LinearLayout progressLoadMoreLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        webView = (WebView) findViewById(R.id.webView);
        searchIconIv = findViewById(R.id.search_icon_iv);
        mSlideViewPager = findViewById(R.id.slide_view_pager);
        mangaIconIv = findViewById(R.id.manga_icon_iv);
        latestEpisodeRV = findViewById(R.id.latest_anime_rv);
        confirmWebView = findViewById(R.id.confirm_webView);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        progressLoadMoreLayout = findViewById(R.id.progress_load_more_layout);

        nestedScrollView.setOnScrollChangeListener(this);
        mangaIconIv.setOnClickListener(this);
        searchIconIv.setOnClickListener(this);

        mSlideIndicator = findViewById(R.id.slide_indicator);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        confirmWebView.getSettings().setJavaScriptEnabled(true);
        confirmWebView.clearHistory();
//        confirmWebView.setVisibility(View.VISIBLE);
//        confirmWebView.loadUrl(LATEST_URL);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        progressBarLayout.setVisibility(View.VISIBLE);

        latestEpisodeRV.setNestedScrollingEnabled(false);
        mGridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150));
        mLatestEpisodeAdapter = new LatestEpisodeAdapter(this);
        latestEpisodeRV.setLayoutManager(mGridLayoutManager);
        latestEpisodeRV.setAdapter(mLatestEpisodeAdapter);

//        new GetAnimeHomePageTask().execute(Constant.LATEST_URL);
        mGetAnimeHomePageTask = new GetAnimeHomePageTask();
        mGetAnimeByPageNumTask = new GetAnimeByPageNumTask();
        mGetAnimeHomePageTask.startTask(Constant.HOME_URL);
    }

    @Override
    protected void onResume() {
        isAutoChangeBanner = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isAutoChangeBanner = false;
        super.onPause();
    }

    @Override
    public void onItemClick(Anime item, int position) {
        AnimeDataManager.getInstance().setAnime(item);
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        startActivity(intent);
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerClick(Anime item, int position) {
        isAutoChangeBanner = false;
        AnimeDataManager.getInstance().setAnime(item);
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        startActivity(intent);
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_icon_iv:
                Intent intent = new Intent(this, SearchAnimeActivity.class);
                intent.putExtra(KEYWORD_ARG, "");
                startActivity(intent);
                break;
            case R.id.manga_icon_iv:
                intent = new Intent(this, MangaFoxActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (v.getChildAt(v.getChildCount() - 1) != null) {
            if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                    scrollY > oldScrollY) {

                int visibleItemCount = mGridLayoutManager.getChildCount();
                int totalItemCount = mGridLayoutManager.getItemCount();
                int pastVisiblesItems = mGridLayoutManager.findFirstVisibleItemPosition();
                if (mCurrentPage < mTotalPage) {

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        mGetAnimeByPageNumTask.startTask(Constant.LATEST_URL + Constant.PAGE_PARAM + (++mCurrentPage));
                        Log.i(TAG, "Load more");
                    }
                }
            }
        }
    }

    private class GetAnimeHomePageTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();

        public void startTask(String url) {
            mGetAnimeHomePageTask.execute(url);
        }

        public void restartTask(String url) {
            mGetAnimeHomePageTask = new GetAnimeHomePageTask();
            mGetAnimeHomePageTask.execute(url);
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
            if (document != null) {
                confirmWebView.stopLoading();
                confirmWebView.setVisibility(View.GONE);

                List<Anime> latestItems = new ArrayList<>();
                latestItems = AnimeParser.getListAnimeItem(document);

                List<Anime> bannerItems = new ArrayList<>();
                bannerItems = AnimeParser.getListBannerAnime(document);

                mTotalPage = AnimeParser.getPaginationAnime(document);
                //If latest page have more than 5 pages, hard code total is 5 pages
                if (mTotalPage > 5) {
                    mTotalPage = 5;
                }

                if (latestItems != null && !latestItems.isEmpty()) {
                    mLatestEpisodeAdapter.setAnimeList(latestItems);
                } else {
                    GetAnimeHomePageTask.this.execute(Constant.HOME_URL);
                    confirmWebView.setVisibility(View.VISIBLE);
                    confirmWebView.loadUrl(Constant.HOME_URL);

                    if (document.selectFirst("h4").text().equals("Nếu bạn là người Việt thì hãy điền thông tin phía bên dưới để xác minh:")) {
                        FormElement confirmForm = (FormElement) document.selectFirst("form");
                        Elements questionSubject = document.select("div.form-group>input");
                        if (questionSubject != null && questionSubject.size() > 0) {
                            questionSubject.get(0).text("20/11");
                            questionSubject.get(1).text("S");

                            // # Now send the form for login
//                            try {
//                                Connection.Response loginActionResponse = confirmForm.submit()
//                                        .userAgent(USER_AGENT)
//                                        .execute();
//                                Log.i(TAG, loginActionResponse.parse().html());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Log.e(TAG, e.getMessage());
//                            }
                        }
                    }
                }

                if (bannerItems != null && !bannerItems.isEmpty()) {
                    // Auto start of viewpager
                    startSlideBanner(bannerItems);
                }

                progressBarLayout.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(HomeActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                restartTask(Constant.HOME_URL);
                confirmWebView.setVisibility(View.VISIBLE);
                confirmWebView.loadUrl(Constant.HOME_URL);
            }
            super.onPostExecute(document);
        }
    }

    private class GetAnimeByPageNumTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();

        public void startTask(String url) {
            mGetAnimeByPageNumTask = new GetAnimeByPageNumTask();
            mGetAnimeByPageNumTask.execute(url);
        }

        public void restartTask(String url) {
            mGetAnimeByPageNumTask = new GetAnimeByPageNumTask();
            mGetAnimeByPageNumTask.execute(url);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressLoadMoreLayout.setVisibility(View.VISIBLE);
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
            if (document != null) {
                confirmWebView.stopLoading();
                confirmWebView.setVisibility(View.GONE);

                List<Anime> moreItems = new ArrayList<>();
                moreItems = AnimeParser.getListAnimeItem(document);

                if (moreItems != null && !moreItems.isEmpty()) {
                    mLatestEpisodeAdapter.addMoreAnime(moreItems);
                } else {
                    restartTask(Constant.HOME_URL);
                    confirmWebView.setVisibility(View.VISIBLE);
                    confirmWebView.loadUrl(Constant.HOME_URL);
                }

                progressLoadMoreLayout.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(HomeActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                restartTask(Constant.HOME_URL);
                confirmWebView.setVisibility(View.VISIBLE);
                confirmWebView.loadUrl(Constant.HOME_URL);
            }
            super.onPostExecute(document);
        }
    }

    private void startSlideBanner(final List<Anime> result) {
        mSlideViewPager.setAdapter(new SlideBannerAdapter(result, this));
        mSlideIndicator.setViewPager(mSlideViewPager);
        isAutoChangeBanner = true;
        final Handler handler = new Handler();
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isAutoChangeBanner) {
                            if (mSlideCurrentPage == result.size()) {
                                mSlideCurrentPage = 0;
                            }
                            mSlideViewPager.setCurrentItem(mSlideCurrentPage++, true);
                        }
                    }
                });
            }
        }, 5000, 5000);
    }

    private class AppWebViewClients extends WebViewClient {
        boolean isRunGetSourceWeb = false;

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
//                try {
//                    String html = URLDecoder.decode(url, "UTF-8").substring(9);
//
//                    Document playerDocument = Jsoup.parse(html);
//                    if (playerDocument != null) {
//                        Episode episode = new Episode();
//                        Element playerSubject = playerDocument.select("div.player").first();
//                        if (playerSubject != null) {
//                            Element videoSubject = playerSubject.getElementsByClass("player-video").first();
//                            if (videoSubject != null) {
//                                Log.d("Direct link: ", videoSubject.attr("src"));
//                                mAnimeSelected.episode.directUrl = videoSubject.attr("src");
//                            }
//                            Element titleSubject = playerSubject.getElementsByClass("player-title").first().getElementsByTag("span").first();
//                            if (titleSubject != null) {
//                                mAnimeSelected.episode.name = titleSubject.text();
//                            }
//                        }
//
//                        Element episodeSelectorSubject = playerDocument.select("div.episode-selector").first();
//                        if (episodeSelectorSubject != null) {
//                            Element inputEpisodeSubject = episodeSelectorSubject.getElementsByTag("input").first();
//                            if (inputEpisodeSubject != null) {
//                                mAnimeSelected.maxEpisode = Integer.parseInt(inputEpisodeSubject.attr("max"));
//                                mAnimeSelected.minEpisode = Integer.parseInt(inputEpisodeSubject.attr("min"));
//                            }
//                        }
//
////                        mAnimeSelected.episode = episode;
//                        if (!TextUtils.isEmpty(mAnimeSelected.episode.directUrl)) {
//                            Intent intent = new Intent(HomeActivity.this, VideoPlayerActivity.class);
//                            intent.putExtra(HomeActivity.ANIME_ARG, mAnimeSelected);
//                            startActivity(intent);
//                            progressDialog.dismiss();
//                        } else {
//                            Toast.makeText(HomeActivity.this, "Can not get link episode", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                    webView.stopLoading();
//                } catch (UnsupportedEncodingException e) {
//                    Log.e("example", "failed to decode source", e);
//                    Toast.makeText(HomeActivity.this, "[" + TAG + "] - " + "Can not get link episode", Toast.LENGTH_LONG).show();
//                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (isRunGetSourceWeb) {
                webView.loadUrl(
                        "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                isRunGetSourceWeb = false;
            }
        }
    }


}
