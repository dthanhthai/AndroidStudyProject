package com.example.doanthanhthai.mangafox;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.doanthanhthai.mangafox.model.Episode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CrawlActivity extends AppCompatActivity {

    public static final String TAG = MangaFoxActivity.class.getSimpleName();
    private WebView webView;

    private static final String URL = "http://vuighe.net/tap-moi-nhat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crawl);

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.clearHistory();
        webView.loadUrl("https://www.google.com.vn/");
        webView.setWebViewClient(new AppWebViewClients());

        new DownloadTask().execute(URL);
    }

    //Download HTML bằng AsynTask
    private class DownloadTask extends AsyncTask<String, Void, ArrayList<Episode>> {

        private static final String TAG = "DownloadTask";

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
        protected void onPostExecute(ArrayList<Episode> articles) {
            super.onPostExecute(articles);
        }
    }

    private class AppWebViewClients extends WebViewClient {

        public AppWebViewClients() {
//            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
//            progress.show();
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            progress.setVisibility(View.GONE);
        }
    }


}
