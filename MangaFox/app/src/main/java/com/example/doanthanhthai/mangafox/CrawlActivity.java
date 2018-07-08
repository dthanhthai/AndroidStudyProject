package com.example.doanthanhthai.mangafox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter;
import com.example.doanthanhthai.mangafox.model.Episode;
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

public class CrawlActivity extends AppCompatActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener, View.OnClickListener {

    public static final String TAG = CrawlActivity.class.getSimpleName();
    private WebView webView;
    private AppWebViewClients webViewClient;
    private ImageView searchIconIv;
    private static final String URL = "http://vuighe.net/tap-moi-nhat";
    private static final String HARD_URL = "http://vuighe.net/otome-wa-boku-ni-koishiteru";
    private RecyclerView latestEpisodeRV;
    private LatestEpisodeAdapter mLatestEpisodeAdapter;
    public static final String EPISODE_URL_ARG = "episodeUrlArg";
    public static final String KEYWORD_ARG = "keywordArg";
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crawl);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        webView = (WebView) findViewById(R.id.webView);
        searchIconIv = findViewById(R.id.search_icon_iv);
        searchIconIv.setOnClickListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
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

        new GetListAnimeTask().execute(URL);
    }

    @Override
    public void onItemClick(Episode item, int position) {
        progressDialog.show();
        webViewClient.setRunGetSourceWeb(true);
        webView.loadUrl(item.url);
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show();

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

    private class GetListAnimeTask extends AsyncTask<String, Void, ArrayList<Episode>> {

        private static final String TAG = "GetListAnimeTask";

        @Override
        protected ArrayList<Episode> doInBackground(String... strings) {
            ArrayList<Episode> listEpisode = new ArrayList<>();
            Document document = null;
            try {
                document = (Document) Jsoup.connect(strings[0]).get();

                if (document != null) {
                    Elements subjectElements = document.select("div.tray-item");
                    if (subjectElements != null && subjectElements.size() > 0) {
                        for (Element element : subjectElements) {
                            Element infoElement = element.getElementsByTag("a").first();
                            Log.i(TAG, "Link: " + infoElement.attr("href"));
                            Episode episode = new Episode();
                            episode.url = "http://vuighe.net" + infoElement.attr("href");

                            if (infoElement != null) {
                                Element imageSubject = infoElement.getElementsByClass("tray-item-thumbnail").first();
                                Element descripteSubject = infoElement.getElementsByClass("tray-item-description").first();

                                if (imageSubject != null) {
                                    episode.image = imageSubject.attr("src");
                                }
                                if (descripteSubject != null) {
                                    Element titleSubject = descripteSubject.getElementsByClass("tray-item-title").first();
                                    if (titleSubject != null) {
                                        episode.title = titleSubject.text();
                                    }
                                    Element nameSubject = descripteSubject.getElementsByClass("tray-item-meta-info").first().getElementsByTag("span").first();
                                    if (nameSubject != null) {
                                        episode.name = nameSubject.text();
                                    }
                                }
                                listEpisode.add(episode);
                            }
                        }
                        Log.i(TAG, "List count: " + listEpisode.size());
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            return listEpisode;
        }

        @Override
        protected void onPostExecute(ArrayList<Episode> result) {
            mLatestEpisodeAdapter.setEpisodeList(result);

            super.onPostExecute(result);
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
                        Element playerSubject = playerDocument.select("div.player").first();
                        if (playerSubject != null) {
                            Element videoSubject = playerSubject.getElementsByClass("player-video").first();
                            if (videoSubject != null) {
                                Log.d("Direct link: ", videoSubject.attr("src"));
                                progressDialog.dismiss();
                                Intent intent = new Intent(CrawlActivity.this, VideoPlayerActivity.class);
                                intent.putExtra(EPISODE_URL_ARG, videoSubject.attr("src"));
                                startActivity(intent);
                                webView.stopLoading();
                            }
                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                }
                webView.getSettings().setJavaScriptEnabled(true);
                return true;
            }
            // For all other links, let the WebView do it's normal thing
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
