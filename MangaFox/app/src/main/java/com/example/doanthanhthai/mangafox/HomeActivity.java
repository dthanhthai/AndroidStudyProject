package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter;
import com.example.doanthanhthai.mangafox.adapter.SlideBannerAdapter;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.parser.AnimeParser;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeActivity extends AppCompatActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener, View.OnClickListener, SlideBannerAdapter.OnSlideBannerAdapterListener {

    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private WebView webView;
    private WebView confirmWebView;
    private AppWebViewClients webViewClient;
    private ImageView searchIconIv;
    private ImageView mangaIconIv;
    private static final String LATEST_URL = "http://animehay.tv/";
    private static final String HARD_URL = "http://vuighe.net/otome-wa-boku-ni-koishiteru";
    private RecyclerView latestEpisodeRV;
    private LatestEpisodeAdapter mLatestEpisodeAdapter;
    public static final String ANIME_ARG = "animeArg";
    public static final String KEYWORD_ARG = "keywordArg";
    private ViewPager mSlideViewPager;
    private CircleIndicator mSlideIndicator;
    private static int mSlideCurrentPage = 0;
    private ProgressDialog progressDialog;
    private Anime mAnimeSelected = null;
    private boolean isAutoChangeBanner = false;

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
        mangaIconIv.setOnClickListener(this);
        searchIconIv.setOnClickListener(this);

        mSlideIndicator = findViewById(R.id.slide_indicator);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        confirmWebView.getSettings().setJavaScriptEnabled(true);
        confirmWebView.clearHistory();
        confirmWebView.setWebViewClient(new ConfirmWebViewClients());
//        confirmWebView.setVisibility(View.VISIBLE);
//        confirmWebView.loadUrl(LATEST_URL);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);


        latestEpisodeRV.setNestedScrollingEnabled(false);
        GridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150));
        mLatestEpisodeAdapter = new LatestEpisodeAdapter(this);
        latestEpisodeRV.setLayoutManager(gridLayoutManager);
        latestEpisodeRV.setAdapter(mLatestEpisodeAdapter);

        new GetAnimeHomePageTask().execute(LATEST_URL);
//        new GetBannerListAnimeTask().execute(LATEST_URL);
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

        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        intent.putExtra(HomeActivity.ANIME_ARG, item);
        startActivity(intent);
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show();

//        progressDialog.show();
//        webViewClient.setRunGetSourceWeb(true);
//        mAnimeSelected = item;
//        webView.loadUrl(item.episode.url);

    }

    @Override
    public void onBannerClick(Anime item, int position) {
        isAutoChangeBanner = false;
        progressDialog.show();
        webViewClient.setRunGetSourceWeb(true);
        mAnimeSelected = item;
        webView.loadUrl(item.episode.getUrl());
        Toast.makeText(this, "Index: " + position, Toast.LENGTH_SHORT).show();
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

    private class GetAnimeHomePageTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            try {
                document = Jsoup.connect(strings[0])
                        .timeout(8 * 1000)
                        .userAgent(USER_AGENT)
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
                List<Anime> latestItems = new ArrayList<>();
                latestItems = AnimeParser.getListAnimeItem(document);

                List<Anime> bannerItems = new ArrayList<>();
                bannerItems = AnimeParser.getListBannerAnime(document);

                if (latestItems != null && !latestItems.isEmpty()) {
                    mLatestEpisodeAdapter.setEpisodeList(latestItems);
                } else {
                    confirmWebView.setVisibility(View.VISIBLE);
                    confirmWebView.loadUrl(LATEST_URL);

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

            } else {
                Toast.makeText(HomeActivity.this, "Cannot get document web", Toast.LENGTH_LONG).show();
                confirmWebView.setVisibility(View.VISIBLE);
                confirmWebView.loadUrl(LATEST_URL);
            }
            super.onPostExecute(document);
        }
    }

//    private class CheckConfirmAnimeTask extends AsyncTask<String, Void, String> {
//        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();
//
//        @Override
//        protected String doInBackground(String... strings) {
//            Document document = null;
//            try {
//                document = (Document) Jsoup.connect(strings[0]).get();
//
//                if (document != null) {
//                    Elements subjectElements = document.select("div.ah-row-film>div.ah-col-film");
//                    if(subjectElements == null){
//                        return strings[0];
//                    }
//
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(TAG, "Parse date fail: " + e.getMessage());
//                return "";
//            }
//            return "";
//        }
//
//        @Override
//        protected void onPostExecute(final String result) {
//            if(!TextUtils.isEmpty(result)){
//                confirmWebView.setVisibility(View.VISIBLE);
//                confirmWebView.loadUrl(result);
//            }
//            super.onPostExecute(result);
//        }
//    }

//    private class GetBannerListAnimeTask extends AsyncTask<String, Void, List<Anime>> {
//        private final String TAG = GetBannerListAnimeTask.class.getSimpleName();
//
//        @Override
//        protected List<Anime> doInBackground(String... strings) {
////            ArrayList<Anime> animeList = new ArrayList<>();
////            Document document = null;
////            try {
////                document = (Document) Jsoup.connect(strings[0]).get();
////
////                if (document != null) {
////                    Elements itemsSub = document.select("div.slider-item");
////                    if (itemsSub != null && itemsSub.size() > 0) {
////                        //Ignore first item
////                        for (int i = 1; i < itemsSub.size(); i++) {
////                            Anime anime = new Anime();
////                            anime.episode = new Episode();
////                            Element rawLinkSub = itemsSub.get(i).getElementsByTag("a").first();
////                            if (rawLinkSub != null) {
////                                String rawUrl = Constant.HOME_URL + rawLinkSub.attr("href");
////
////                                StringTokenizer st = new StringTokenizer(rawUrl, "/");
////                                List<String> rawLinkItems = new ArrayList<>();
////                                while (st.hasMoreTokens()) {
////                                    rawLinkItems.add(st.nextToken());
////                                }
////                                anime.url = rawLinkItems.get(0) + "//" + rawLinkItems.get(1) + "/" + rawLinkItems.get(2);
////                                anime.episode.url = rawUrl;
////
////                                st = new StringTokenizer(rawLinkItems.get(3), "-");
////                                List<String> nameItems = new ArrayList<>();
////                                while (st.hasMoreTokens()) {
////                                    nameItems.add(st.nextToken());
////                                }
////                                try {
////                                    anime.episode.curNum = Integer.parseInt(nameItems.get(1));
////                                } catch (NumberFormatException ex) {
////                                    anime.episode.curNum = -1;
////                                    Log.e(TAG, ex.getMessage());
////                                }
////
////                                Element imageSub = rawLinkSub.getElementsByTag("img").first();
////                                if (imageSub != null) {
////                                    anime.bannerImage = imageSub.attr("src");
////                                }
////                                animeList.add(anime);
////                            }
////                        }
////                    }
////                }
////
////            } catch (IOException e) {
////                e.printStackTrace();
////                Log.e(TAG, "Parse date fail: " + e.getMessage());
////            }
////            return animeList;
//
//            return AnimeParser.getListBannerAnime(strings[0]);
//        }
//
//        @Override
//        protected void onPostExecute(final List<Anime> result) {
//            // Auto start of viewpager
//            startSlideBanner(result);
//
//            super.onPostExecute(result);
//        }
//    }

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
                        Episode episode = new Episode();
                        Element playerSubject = playerDocument.select("div.player").first();
                        if (playerSubject != null) {
                            Element videoSubject = playerSubject.getElementsByClass("player-video").first();
                            if (videoSubject != null) {
                                Log.d("Direct link: ", videoSubject.attr("src"));
                                mAnimeSelected.episode.setDirectUrl(videoSubject.attr("src"));
                            }
                            Element titleSubject = playerSubject.getElementsByClass("player-title").first().getElementsByTag("span").first();
                            if (titleSubject != null) {
                                mAnimeSelected.episode.setName(titleSubject.text());
                            }
                        }

                        Element episodeSelectorSubject = playerDocument.select("div.episode-selector").first();
                        if (episodeSelectorSubject != null) {
                            Element inputEpisodeSubject = episodeSelectorSubject.getElementsByTag("input").first();
                            if (inputEpisodeSubject != null) {
                                mAnimeSelected.maxEpisode = Integer.parseInt(inputEpisodeSubject.attr("max"));
                                mAnimeSelected.minEpisode = Integer.parseInt(inputEpisodeSubject.attr("min"));
                            }
                        }

//                        mAnimeSelected.episode = episode;
                        if (!TextUtils.isEmpty(mAnimeSelected.episode.getDirectUrl())) {
                            Intent intent = new Intent(HomeActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(HomeActivity.ANIME_ARG, mAnimeSelected);
                            startActivity(intent);
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(HomeActivity.this, "Can not get link episode", Toast.LENGTH_LONG).show();
                        }
                    }
                    webView.stopLoading();
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                    Toast.makeText(HomeActivity.this, "[" + TAG + "] - " + "Can not get link episode", Toast.LENGTH_LONG).show();
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
                        "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                isRunGetSourceWeb = false;
            }
        }
    }

    private class ConfirmWebViewClients extends WebViewClient {
        boolean isRunGetSourceWeb = false;

        public ConfirmWebViewClients() {
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

            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            if (isRunGetSourceWeb) {
//                webView.loadUrl(
//                        "javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
//                isRunGetSourceWeb = false;
//            }
        }
    }

}
