package com.example.doanthanhthai.mangafox;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager;
import com.example.doanthanhthai.mangafox.widget.StartSnapHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CrawlActivity extends AppCompatActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener {

    public static final String TAG = CrawlActivity.class.getSimpleName();

    private static final String URL = "http://vuighe.net/tap-moi-nhat";
    private RecyclerView latestEpisodeRV;
    private LatestEpisodeAdapter mLatestEpisodeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crawl);

        latestEpisodeRV = findViewById(R.id.latest_anime_rv);

        GridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, 400);
        mLatestEpisodeAdapter = new LatestEpisodeAdapter(this);
        latestEpisodeRV.setLayoutManager(gridLayoutManager);
        latestEpisodeRV.setAdapter(mLatestEpisodeAdapter);

//        SnapHelper startSnapHelper = new StartSnapHelper();
//        startSnapHelper.attachToRecyclerView(latestEpisodeRV);

        new DownloadTask().execute(URL);
    }

    @Override
    public void onItemClick(Episode item, int position) {
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show();
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
            mLatestEpisodeAdapter.setEpisodeList(articles);

            super.onPostExecute(articles);
        }
    }
}
