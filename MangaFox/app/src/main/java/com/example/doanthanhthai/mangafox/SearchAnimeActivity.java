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
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.adapter.ResultAnimeAdapter;
import com.example.doanthanhthai.mangafox.model.Anime;
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

public class SearchAnimeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, ResultAnimeAdapter.OnResultAnimeAdapterListener {
    private static final String TAG = SearchAnimeActivity.class.getSimpleName();
    private WebView webView;
    private AppWebViewClients webViewClient;
    private MenuItem searchMenu;
    private SearchView searchView;
    private ProgressDialog progressDialog;
    private RecyclerView resultAnimeRv;
    private ResultAnimeAdapter mResultAnimeAdapter;
    private Anime mAnimeSelected = null;
    private TextView emptyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_anime);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        searchView = findViewById(R.id.anime_search_view);
        resultAnimeRv = findViewById(R.id.result_anime_rv);
        webView = (WebView) findViewById(R.id.webView);
        emptyTv = findViewById(R.id.empty_result_tv);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        searchView.onActionViewExpanded();
        searchView.requestFocus();
        searchView.setOnQueryTextListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Data loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        GridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150));
        mResultAnimeAdapter = new ResultAnimeAdapter(this);
        resultAnimeRv.setLayoutManager(gridLayoutManager);
        resultAnimeRv.setAdapter(mResultAnimeAdapter);

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        emptyTv.setVisibility(View.GONE);
        progressDialog.show();
        new GetListAnimeTask().execute(Constant.SEARCH_URL + query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(Anime item, int position) {
        progressDialog.show();
        webViewClient.setRunGetSourceWeb(true);
        mAnimeSelected = item;
        webView.loadUrl(item.url);
        Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show();
    }


    private class GetListAnimeTask extends AsyncTask<String, Void, ArrayList<Anime>> {

        private static final String TAG = "GetListAnimeTask";

        @Override
        protected ArrayList<Anime> doInBackground(String... strings) {
            ArrayList<Anime> listEpisode = new ArrayList<>();
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
                            anime.url = Constant.HOME_URL + infoElement.attr("href");

                            if (infoElement != null) {
                                Element imageSubject = infoElement.getElementsByClass("tray-item-thumbnail").first();
                                Element descripteSubject = infoElement.getElementsByClass("tray-item-description").first();

                                if (imageSubject != null) {
                                    anime.image = imageSubject.attr("src");
                                }
                                if (descripteSubject != null) {
                                    Element titleSubject = descripteSubject.getElementsByClass("tray-item-title").first();
                                    if (titleSubject != null) {
                                        anime.title = titleSubject.text();
                                    }
                                }
                                listEpisode.add(anime);
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
        protected void onPostExecute(ArrayList<Anime> result) {
            progressDialog.dismiss();
            if (result.isEmpty()) {
                emptyTv.setVisibility(View.VISIBLE);
            } else {
                mResultAnimeAdapter.setEpisodeList(result);
                searchView.clearFocus();
            }
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
                        Episode episode = new Episode();
                        Element playerSubject = playerDocument.select("div.player").first();
                        if (playerSubject != null) {
                            Element videoSubject = playerSubject.getElementsByClass("player-video").first();
                            if (videoSubject != null) {
                                Log.d("Direct link: ", videoSubject.attr("src"));
                                episode.url = videoSubject.attr("src");
                            }

                            Element titleSubject = playerSubject.getElementsByClass("player-title").first().getElementsByTag("span").first();
                            if (titleSubject != null) {
                                episode.name = titleSubject.text();
                            }
                        }

//                        Element titleSubject = playerDocument.selectFirst("h1.film-info-title");
//                        if (titleSubject != null) {
//                            episode.name = titleSubject.text();
//                        }

                        Element episodeSelectorSubject = playerDocument.select("div.episode-selector").first();
                        if (episodeSelectorSubject != null) {
                            Element inputEpisodeSubject = episodeSelectorSubject.getElementsByTag("input").first();
                            if (inputEpisodeSubject != null) {
                                mAnimeSelected.maxEpisode = Integer.parseInt(inputEpisodeSubject.attr("max"));
                                mAnimeSelected.minEpisode = Integer.parseInt(inputEpisodeSubject.attr("min"));
                            }
                        }
                        mAnimeSelected.episode = episode;
                        if (!TextUtils.isEmpty(mAnimeSelected.episode.url)) {
                            Intent intent = new Intent(SearchAnimeActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(CrawlActivity.ANIME_ARG, mAnimeSelected);
                            startActivity(intent);
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(SearchAnimeActivity.this, "Can not get link episode", Toast.LENGTH_LONG).show();
                        }
                    }
                    webView.stopLoading();
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                    Toast.makeText(SearchAnimeActivity.this, "Can not get link episode", Toast.LENGTH_LONG).show();
                }
//                webView.getSettings().setJavaScriptEnabled(true);
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
