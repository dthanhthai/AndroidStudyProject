package com.example.doanthanhthai.mangafox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanthanhthai.mangafox.adapter.LatestEpisodeAdapter;
import com.example.doanthanhthai.mangafox.adapter.NavigationAdapter;
import com.example.doanthanhthai.mangafox.adapter.SlideBannerAdapter;
import com.example.doanthanhthai.mangafox.base.BaseActivity;
import com.example.doanthanhthai.mangafox.fragment.AnimeGenreFragment;
import com.example.doanthanhthai.mangafox.fragment.AnimeYearFragment;
import com.example.doanthanhthai.mangafox.fragment.CNGenreFragment;
import com.example.doanthanhthai.mangafox.fragment.CartoonFragment;
import com.example.doanthanhthai.mangafox.fragment.SettingFragment;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.NavigationModel;
import com.example.doanthanhthai.mangafox.repository.AnimeRepository;
import com.example.doanthanhthai.mangafox.share.Constant;
import com.example.doanthanhthai.mangafox.share.DynamicColumnHelper;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;
import com.example.doanthanhthai.mangafox.widget.ProgressAnimeView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeActivity extends BaseActivity implements LatestEpisodeAdapter.OnLatestEpisodeAdapterListener,
        View.OnClickListener, SlideBannerAdapter.OnSlideBannerAdapterListener, NestedScrollView.OnScrollChangeListener, NavigationAdapter.NavigationAdapterListener {

    public static final String TAG = HomeActivity.class.getSimpleName();
    public static final String ANIME_ARG = "animeArg";
    public static final String KEYWORD_ARG = "keywordArg";

    private WebView webView;
    private WebView confirmWebView;
    private AppWebViewClients webViewClient;
    private ImageView searchIconIv, favoriteIconIv, mangaIconIv;
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
    private ProgressAnimeView progressFullLayout;
    private LinearLayout progressLoadMoreLayout;
    private Toolbar mToolbar;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    private CastStateListener mCastStateListener;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView navigationRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        preConfig(savedInstanceState);
        mapView();
        initData();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        isAutoChangeBanner = true;
        mCastContext.addCastStateListener(mCastStateListener);
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        isAutoChangeBanner = false;
        mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(mTaskHandler != null){
//            mTaskHandler.removeCallbacksAndMessages(null);
//            mTaskHandler = null;
//        }
        if (mGetAnimeHomePageTask != null) {
            mGetAnimeHomePageTask.cancel(true);
            mGetAnimeHomePageTask = null;
        }
        if (mGetAnimeByPageNumTask != null) {
            mGetAnimeByPageNumTask.cancel(true);
            mGetAnimeByPageNumTask = null;
        }

        Log.d(TAG, "onDestroy");
    }

    @Override
    public void preConfig(Bundle savedInstanceState) {
        super.preConfig(savedInstanceState);
    }

    @Override
    public void mapView() {
        super.mapView();
        webView = (WebView) findViewById(R.id.webView);
        searchIconIv = findViewById(R.id.search_icon_iv);
        mSlideViewPager = findViewById(R.id.slide_view_pager);
//        mangaIconIv = findViewById(R.id.manga_icon_iv);
        latestEpisodeRV = findViewById(R.id.latest_anime_rv);
        confirmWebView = findViewById(R.id.confirm_webView);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        progressFullLayout = findViewById(R.id.progress_full_screen_view);
        progressLoadMoreLayout = findViewById(R.id.progress_load_more_layout);
        favoriteIconIv = findViewById(R.id.favorite_icon_iv);
        mSlideIndicator = findViewById(R.id.slide_indicator);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationRv = findViewById(R.id.rvNavigation);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void initData() {
        super.initData();
        nestedScrollView.setOnScrollChangeListener(this);
//        mangaIconIv.setOnClickListener(this);
        searchIconIv.setOnClickListener(this);
        favoriteIconIv.setOnClickListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(true);
        webView.getSettings().setLoadsImagesAutomatically(false);
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

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        mCastContext = CastContext.getSharedInstance(this);

        progressFullLayout.setVisibility(View.VISIBLE);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setIcon(android.R.color.transparent);

        List<NavigationModel> navigationModelList = new ArrayList<>();
        navigationModelList.add(new NavigationModel(Constant.FAVORITE_NAVIGATION_ID, R.drawable.ic_arrow_left, "Favorite", false));
        navigationModelList.add(new NavigationModel(Constant.ANIME_GENRE_NAVIGATION_ID, R.drawable.ic_arrow_left, "Anime", false));
        navigationModelList.add(new NavigationModel(Constant.CN_GENRE_NAVIGATION_ID, R.drawable.ic_arrow_left, "CN Animation", false));
        navigationModelList.add(new NavigationModel(Constant.YEAR_NAVIGATION_ID, R.drawable.ic_arrow_left, "Year", false));
        navigationModelList.add(new NavigationModel(Constant.CARTOON_NAVIGATION_ID, R.drawable.ic_arrow_left, "Cartoon", false));
        navigationModelList.add(new NavigationModel(Constant.SETTING_NAVIGATION_ID, R.drawable.ic_arrow_left, "Setting", false));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        NavigationAdapter navigationAdapter = new NavigationAdapter(navigationModelList, this);
        navigationRv.setLayoutManager(linearLayoutManager);
        navigationRv.setAdapter(navigationAdapter);

        latestEpisodeRV.setNestedScrollingEnabled(false);
//        mGridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 150));
        mLatestEpisodeAdapter = new LatestEpisodeAdapter(this);
        DynamicColumnHelper dynamicColumnHelper = new DynamicColumnHelper(this);

        mLatestEpisodeAdapter.setDynamicColumnHelper(dynamicColumnHelper);
        mGridLayoutManager = new GridLayoutManager(this, dynamicColumnHelper.getColNum(), RecyclerView.VERTICAL, false);
        latestEpisodeRV.setLayoutManager(mGridLayoutManager);
        latestEpisodeRV.setAdapter(mLatestEpisodeAdapter);
//        latestEpisodeRV.addItemDecoration(new GridSpacingItemDecoration(colNum, spacing, false, 0));

//        new GetAnimeHomePageTask().execute(Constant.LATEST_URL);
        mGetAnimeHomePageTask = new GetAnimeHomePageTask();
        mGetAnimeByPageNumTask = new GetAnimeByPageNumTask();

        List<Anime> latestItems = AnimeDataManager.getInstance().getHomeList();
        List<Anime> bannerItems = AnimeDataManager.getInstance().getBannerList();
        mTotalPage = AnimeDataManager.getInstance().getHomeTotalPage();
        if (latestItems != null && !latestItems.isEmpty() && bannerItems != null && !bannerItems.isEmpty()) {
            mLatestEpisodeAdapter.setAnimeList(latestItems);
            startSlideBanner(bannerItems);
            AnimeDataManager.getInstance().setHomeList(null);
            AnimeDataManager.getInstance().setBannerList(null);
            //Scroll to top
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    nestedScrollView.scrollTo(0, 0);
                }
            },100);
            progressFullLayout.setVisibility(View.GONE);
        } else {
            mGetAnimeHomePageTask.startTask(Constant.HOME_URL);
        }
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        setSupportActionBar(mToolbar);
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
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return true;
    }

    @Override
    public void onItemClick(Anime item, int position) {
        AnimeDataManager.getInstance().setAnime(item);

        LatestEpisodeAdapter.LatestViewHolder viewHolder =
                (LatestEpisodeAdapter.LatestViewHolder) latestEpisodeRV.findViewHolderForPosition(position);
        Pair<View, String> imagePair = Pair
                .create((View) viewHolder.getPosterImg(), getString(R.string.transition_image));
        Pair<View, String> titlePair = Pair
                .create((View) viewHolder.getAnimeTitleTv(), getString(R.string.transition_title));
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imagePair, titlePair);
        if (viewHolder.getPosterImg().getDrawable() instanceof BitmapDrawable) {
            AnimeDataManager.getInstance().setThumbnailBitmap(((BitmapDrawable) viewHolder.getPosterImg().getDrawable()).getBitmap());
        } else {
            AnimeDataManager.getInstance().setThumbnailBitmap(null);
        }
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        startActivity(intent, options.toBundle());
//        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerClick(Anime item, int position, View view) {
        isAutoChangeBanner = false;
        AnimeDataManager.getInstance().setAnime(item);

        TextView titleTv = view.findViewById(R.id.anime_title);
        ActivityOptionsCompat options = null;
        if (titleTv != null) {
            Pair<View, String> titlePair = Pair
                    .create((View) titleTv, getString(R.string.transition_title));
            options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, titlePair);
        }
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        if (options != null) {
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.search_icon_iv:
                intent = new Intent(this, SearchAnimeActivity.class);
                intent.putExtra(KEYWORD_ARG, "");
                startActivity(intent);
                break;
//            case R.id.manga_icon_iv:
//                intent = new Intent(this, MangaFoxActivity.class);
//                startActivity(intent);
//                break;
            case R.id.favorite_icon_iv:
                intent = new Intent(this, FavoriteActivity.class);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClicked(@NotNull NavigationModel item) {
        Fragment fragment = null;
        String fragmentTag = "";
        drawerLayout.closeDrawers();
        switch (item.getId()) {
            case Constant.FAVORITE_NAVIGATION_ID:
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
                Toast.makeText(HomeActivity.this, "FAVORITE_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            case Constant.ANIME_GENRE_NAVIGATION_ID:
                fragment = new AnimeGenreFragment();
                fragmentTag = AnimeGenreFragment.TAG;
                Toast.makeText(HomeActivity.this, "ANIME_GENRE_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            case Constant.CN_GENRE_NAVIGATION_ID:
                fragment = new CNGenreFragment();
                fragmentTag = CNGenreFragment.TAG;
                Toast.makeText(HomeActivity.this, "CN_GENRE_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            case Constant.YEAR_NAVIGATION_ID:
                fragment = new AnimeYearFragment();
                fragmentTag = AnimeYearFragment.TAG;
                Toast.makeText(HomeActivity.this, "YEAR_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            case Constant.SETTING_NAVIGATION_ID:
                fragment = new SettingFragment();
                fragmentTag = SettingFragment.TAG;
                Toast.makeText(HomeActivity.this, "SETTING_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            case Constant.CARTOON_NAVIGATION_ID:
                fragment = new CartoonFragment();
                fragmentTag = CartoonFragment.TAG;
                Toast.makeText(HomeActivity.this, "CARTOON_NAVIGATION_ID", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        if (fragment != null && !TextUtils.isEmpty(fragmentTag)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                    .replace(R.id.fragment_container, fragment, fragmentTag)
                    .addToBackStack(fragmentTag)
                    .commit();
        }
    }

    private class GetAnimeHomePageTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();

        public void startTask(String url) {
            mGetAnimeHomePageTask.execute(url);
        }

        public void restartTask(String url) {
            if (mGetAnimeHomePageTask != null) {
                mGetAnimeHomePageTask.cancel(true);
                mGetAnimeHomePageTask = null;
            }

            mGetAnimeHomePageTask = new GetAnimeHomePageTask();
            mGetAnimeHomePageTask.execute(url);
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            Map<String, String> webCookies = PreferenceHelper.getInstance(HomeActivity.this).getCookie();
            try {
//                if (webCookies != null && !webCookies.isEmpty()) {
                Connection.Response response = Jsoup.connect(strings[0])
                        .timeout(Constant.INSTANCE.getTIME_OUT())
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute();
//                } else {
//                    response = Jsoup.connect(strings[0])
//                            .timeout(Constant.INSTANCE.getTIME_OUT())
//                            .userAgent(Constant.USER_AGENT)
//                            .execute();
//                }
                document = response.parse();

                webCookies = response.cookies();
                PreferenceHelper.getInstance(HomeActivity.this).saveCookie(webCookies);

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
                latestItems = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListAnimeItem(document);

                List<Anime> bannerItems = new ArrayList<>();
                bannerItems = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListBannerAnime(document);

                mTotalPage = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getPaginationAnime(document);
                //If latest page have more than 5 pages, hard code total is 5 pages
                if (mTotalPage > 5) {
                    mTotalPage = 5;
                }
                Log.d(TAG, "onPostExecute");
                if (latestItems != null && !latestItems.isEmpty()) {
                    mLatestEpisodeAdapter.setAnimeList(latestItems);
                } else {
                    Toast.makeText(HomeActivity.this, "Got confirm web, try again", Toast.LENGTH_SHORT).show();
                    mTaskHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restartTask(Constant.HOME_URL);
                        }
                    }, 3 * 1000);
//                    confirmWebView.setVisibility(View.VISIBLE);
//                    confirmWebView.loadUrl(Constant.HOME_URL);
                }

                if (bannerItems != null && !bannerItems.isEmpty()) {
                    // Auto start of viewpager
                    startSlideBanner(bannerItems);
                }

                progressFullLayout.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(HomeActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_SHORT).show();
                mTaskHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartTask(Constant.HOME_URL);
                    }
                }, 3 * 1000);
//                confirmWebView.setVisibility(View.VISIBLE);
//                confirmWebView.loadUrl(Constant.HOME_URL);
            }
            super.onPostExecute(document);
        }
    }

    private class GetAnimeByPageNumTask extends AsyncTask<String, Void, Document> {
        private final String TAG = GetAnimeHomePageTask.class.getSimpleName();

        public void startTask(String url) {
            if (mGetAnimeByPageNumTask != null) {
                mGetAnimeByPageNumTask.cancel(true);
                mGetAnimeByPageNumTask = null;
            }

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
            Map<String, String> webCookies = PreferenceHelper.getInstance(HomeActivity.this).getCookie();
            try {
                Connection.Response response = Jsoup.connect(strings[0])
                        .timeout(Constant.INSTANCE.getTIME_OUT())
                        .userAgent(Constant.USER_AGENT)
                        .cookies(webCookies)
                        .execute();
                document = response.parse();

                webCookies = response.cookies();
                PreferenceHelper.getInstance(HomeActivity.this).saveCookie(webCookies);
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
                moreItems = new AnimeRepository(AnimeRepository.WEB_TYPE.ANIMEHAY).getListAnimeItem(document);

                if (moreItems != null && !moreItems.isEmpty()) {
                    mLatestEpisodeAdapter.addMoreAnime(moreItems);
                } else {
                    mTaskHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restartTask(Constant.HOME_URL);
                        }
                    }, 3 * 1000);
                    confirmWebView.setVisibility(View.VISIBLE);
                    confirmWebView.loadUrl(Constant.HOME_URL);
                }

                progressLoadMoreLayout.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "Cannot get DOCUMENT web");
                Toast.makeText(HomeActivity.this, "Cannot get DOCUMENT web", Toast.LENGTH_LONG).show();
                mTaskHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartTask(Constant.HOME_URL);
                    }
                }, 3 * 1000);
                confirmWebView.setVisibility(View.VISIBLE);
                confirmWebView.loadUrl(Constant.HOME_URL);
            }
            super.onPostExecute(document);
        }
    }

    private void startSlideBanner(final List<Anime> result) {
        SlideBannerAdapter slideBannerAdapter = new SlideBannerAdapter(result, this);
        mSlideViewPager.setAdapter(slideBannerAdapter);
        mSlideIndicator.setViewPager(mSlideViewPager);

        mSlideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSlideCurrentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            HomeActivity.this, mediaRouteMenuItem)
                            .setTitleText("Introducing Cast")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }
}
