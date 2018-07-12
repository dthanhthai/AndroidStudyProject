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
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class CrawlActivity extends AppCompatActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener, View.OnClickListener, SlideBannerAdapter.OnSlideBannerAdapterListener {

    public static final String TAG = CrawlActivity.class.getSimpleName();
    private WebView webView;
    private AppWebViewClients webViewClient;
    private ImageView searchIconIv;
    private static final String URL = "http://vuighe.net/tap-moi-nhat";
    private static final String HARD_URL = "http://vuighe.net/otome-wa-boku-ni-koishiteru";
    private RecyclerView latestEpisodeRV;
    private LatestEpisodeAdapter mLatestEpisodeAdapter;
    public static final String ANIME_ARG = "episodeUrlArg";
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
        setContentView(R.layout.activity_crawl);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        webView = (WebView) findViewById(R.id.webView);
        searchIconIv = findViewById(R.id.search_icon_iv);
        mSlideViewPager = findViewById(R.id.slide_view_pager);
        searchIconIv.setOnClickListener(this);

        mSlideIndicator = findViewById(R.id.slide_indicator);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);


        latestEpisodeRV = findViewById(R.id.latest_anime_rv);

        GridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150));
        mLatestEpisodeAdapter = new LatestEpisodeAdapter(this);
        latestEpisodeRV.setLayoutManager(gridLayoutManager);
        latestEpisodeRV.setAdapter(mLatestEpisodeAdapter);

        new GetLatestListAnimeTask().execute(URL);
        new GetBannerListAnimeTask().execute(Constant.HOME_URL);
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
        progressDialog.show();
        webViewClient.setRunGetSourceWeb(true);
        mAnimeSelected = item;
        webView.loadUrl(item.episode.url);
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBannerClick(Anime item, int position) {
        isAutoChangeBanner = false;
        progressDialog.show();
        webViewClient.setRunGetSourceWeb(true);
        mAnimeSelected = item;
        webView.loadUrl(item.episode.url);
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
        }
    }

    private class GetLatestListAnimeTask extends AsyncTask<String, Void, ArrayList<Anime>> {
        private final String TAG = GetLatestListAnimeTask.class.getSimpleName();

        @Override
        protected ArrayList<Anime> doInBackground(String... strings) {
            ArrayList<Anime> animeList = new ArrayList<>();
            Document document = null;
            try {
                document = (Document) Jsoup.connect(strings[0]).get();

                if (document != null) {
                    Elements subjectElements = document.select("div.tray-item");
                    if (subjectElements != null && subjectElements.size() > 0) {
                        for (Element element : subjectElements) {
                            Element infoElement = element.getElementsByTag("a").first();
                            Log.i(TAG, "Link: " + infoElement.attr("href"));
                            Anime anime = new Anime();
                            anime.episode = new Episode();
                            String rawUrl = Constant.HOME_URL + infoElement.attr("href");

                            StringTokenizer st = new StringTokenizer(rawUrl, "/");
                            List<String> rawLinkItems = new ArrayList<>();
                            while (st.hasMoreTokens()) {
                                rawLinkItems.add(st.nextToken());
                            }
                            anime.url = rawLinkItems.get(0) + "//" + rawLinkItems.get(1) + "/" + rawLinkItems.get(2);
                            anime.episode.url = rawUrl;

                            st = new StringTokenizer(rawLinkItems.get(3), "-");
                            List<String> nameItems = new ArrayList<>();
                            while (st.hasMoreTokens()) {
                                nameItems.add(st.nextToken());
                            }
                            try {
                                anime.episode.curNum = Integer.parseInt(nameItems.get(1));
                            } catch (NumberFormatException ex) {
                                anime.episode.curNum = -1;
                                Log.e(TAG, ex.getMessage());
                            }


                            if (infoElement != null) {
                                Element imageSubject = infoElement.getElementsByClass("tray-item-thumbnail").first();
                                Element descriptionSubject = infoElement.getElementsByClass("tray-item-description").first();
                                Element upcomingSubject = infoElement.getElementsByClass("tray-item-upcoming").first();

                                //Don't add upcoming episode into list
                                if (upcomingSubject != null) {
                                    continue;
                                }
                                if (imageSubject != null) {
                                    anime.image = imageSubject.attr("src");
                                }
                                if (descriptionSubject != null) {
                                    Element titleSubject = descriptionSubject.getElementsByClass("tray-item-title").first();
                                    if (titleSubject != null) {
                                        anime.title = titleSubject.text();
                                    }
                                    Element nameSubject = descriptionSubject.getElementsByClass("tray-item-meta-info").first().getElementsByTag("span").first();
                                    if (nameSubject != null) {
                                        anime.episode.name = nameSubject.text();

                                        //Get episode number
//                                        if (anime.episode.name.contains("Tập")) {
//                                            if (anime.episode.name.contains("-")) {
//                                                String tmp = anime.episode.name.substring(0, anime.episode.name.indexOf("-")).trim();
//                                                anime.episode.curNum = Integer.parseInt(tmp.toLowerCase().replace("tập", "").trim());
//                                            }
//                                        } else {
//                                            anime.episode.curNum = -1;
//                                        }
                                    }
                                }
                                animeList.add(anime);
                            }
                        }
                        Log.i(TAG, "List count: " + animeList.size());
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Parse date fail: " + e.getMessage());
            }
            return animeList;
        }

        @Override
        protected void onPostExecute(final ArrayList<Anime> result) {
            mLatestEpisodeAdapter.setEpisodeList(result);
            super.onPostExecute(result);
        }
    }

    private class GetBannerListAnimeTask extends AsyncTask<String, Void, ArrayList<Anime>> {
        private final String TAG = GetBannerListAnimeTask.class.getSimpleName();

        @Override
        protected ArrayList<Anime> doInBackground(String... strings) {
            ArrayList<Anime> animeList = new ArrayList<>();
            Document document = null;
            try {
                document = (Document) Jsoup.connect(strings[0]).get();

                if (document != null) {
                    Elements itemsSub = document.select("div.slider-item");
                    if (itemsSub != null && itemsSub.size() > 0) {
                        //Ignore first item
                        for (int i = 1; i < itemsSub.size(); i++) {
                            Anime anime = new Anime();
                            anime.episode = new Episode();
                            Element rawLinkSub = itemsSub.get(i).getElementsByTag("a").first();
                            if (rawLinkSub != null) {
                                String rawUrl = Constant.HOME_URL + rawLinkSub.attr("href");

                                StringTokenizer st = new StringTokenizer(rawUrl, "/");
                                List<String> rawLinkItems = new ArrayList<>();
                                while (st.hasMoreTokens()) {
                                    rawLinkItems.add(st.nextToken());
                                }
                                anime.url = rawLinkItems.get(0) + "//" + rawLinkItems.get(1) + "/" + rawLinkItems.get(2);
                                anime.episode.url = rawUrl;

                                st = new StringTokenizer(rawLinkItems.get(3), "-");
                                List<String> nameItems = new ArrayList<>();
                                while (st.hasMoreTokens()) {
                                    nameItems.add(st.nextToken());
                                }
                                try {
                                    anime.episode.curNum = Integer.parseInt(nameItems.get(1));
                                } catch (NumberFormatException ex) {
                                    anime.episode.curNum = -1;
                                    Log.e(TAG, ex.getMessage());
                                }

                                Element imageSub = rawLinkSub.getElementsByTag("img").first();
                                if (imageSub != null) {
                                    anime.bannerImage = imageSub.attr("src");
                                }
                                animeList.add(anime);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Parse date fail: " + e.getMessage());
            }
            return animeList;
        }

        @Override
        protected void onPostExecute(final ArrayList<Anime> result) {
            // Auto start of viewpager
            startSlideBanner(result);

            super.onPostExecute(result);
        }
    }

    private void startSlideBanner(final ArrayList<Anime> result) {
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
                        if(isAutoChangeBanner) {
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
                                mAnimeSelected.episode.directUrl = videoSubject.attr("src");
                            }
                            Element titleSubject = playerSubject.getElementsByClass("player-title").first().getElementsByTag("span").first();
                            if (titleSubject != null) {
                                mAnimeSelected.episode.name = titleSubject.text();
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
                        if (!TextUtils.isEmpty(mAnimeSelected.episode.directUrl)) {
                            Intent intent = new Intent(CrawlActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(CrawlActivity.ANIME_ARG, mAnimeSelected);
                            startActivity(intent);
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(CrawlActivity.this, "Can not get link episode", Toast.LENGTH_LONG).show();
                        }
                    }
                    webView.stopLoading();
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                    Toast.makeText(CrawlActivity.this, "[" + TAG + "] - " + "Can not get link episode", Toast.LENGTH_LONG).show();
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


}
