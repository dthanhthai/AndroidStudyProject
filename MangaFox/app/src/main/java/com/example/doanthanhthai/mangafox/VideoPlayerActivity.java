package com.example.doanthanhthai.mangafox;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.doanthanhthai.mangafox.adapter.NumberEpisodeAdapter;
import com.example.doanthanhthai.mangafox.manager.AnimeDataManager;
import com.example.doanthanhthai.mangafox.model.Anime;
import com.example.doanthanhthai.mangafox.model.Episode;
import com.example.doanthanhthai.mangafox.parser.AnimeParser;
import com.example.doanthanhthai.mangafox.share.PreferenceHelper;
import com.example.doanthanhthai.mangafox.share.Utils;
import com.example.doanthanhthai.mangafox.widget.AutoFitGridLayoutManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity implements NumberEpisodeAdapter.OnNumberEpisodeAdapterListener, Player.EventListener, View.OnClickListener {
    private static final String TAG = VideoPlayerActivity.class.getSimpleName();
    private Anime mCurrentAnime;

    private WebView webView;
    private AppWebViewClients webViewClient;

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    protected String userAgent;
    private MediaSource mediaSource;
    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private TextView animeTitleTv, toolbarTitleTv;
    private TextView episodeNameTv;
    private RecyclerView numberEpisodeRv;
    private FrameLayout progressBarLayout;
    private TextView errorMsgPlayerTv;
    private ImageView coverPlayerIv;
    private ImageView backBtn;
    private NumberEpisodeAdapter mNumberEpisodeAdapter;
    private Dialog mFullScreenDialog;
    private int mResumeWindow;
    private long mResumePosition;
    private int indexPlayingItem = 0;

    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private final String STATE_INDEX_PLAYING_ITEM = "indexPlayItem";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Remove notification bar
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_player);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            indexPlayingItem = savedInstanceState.getInt(STATE_INDEX_PLAYING_ITEM);
        }

        mExoPlayerView = findViewById(R.id.exoplayer);
        numberEpisodeRv = findViewById(R.id.number_episode_rv);
        webView = (WebView) findViewById(R.id.webView);
        animeTitleTv = findViewById(R.id.anime_title_tv);
        episodeNameTv = findViewById(R.id.episode_name_tv);
        backBtn = findViewById(R.id.toolbar_back_btn);
        toolbarTitleTv = findViewById(R.id.toolbar_title);

        backBtn.setOnClickListener(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearHistory();
        webViewClient = new AppWebViewClients();
        webView.setWebViewClient(webViewClient);

        GridLayoutManager gridLayoutManager = new AutoFitGridLayoutManager(this, Utils.convertDpToPixel(this, 60));
        mNumberEpisodeAdapter = new NumberEpisodeAdapter(this);
        numberEpisodeRv.setLayoutManager(gridLayoutManager);
        numberEpisodeRv.setAdapter(mNumberEpisodeAdapter);
        numberEpisodeRv.setNestedScrollingEnabled(false);

        mExoPlayerView.setErrorMessageProvider(new PlayerErrorMessageProvider());

        mCurrentAnime = AnimeDataManager.getInstance().getAnime();
        if (mCurrentAnime == null) {
            Toast.makeText(VideoPlayerActivity.this, "[" + TAG + "] - " + "Don't have direct link!!!", Toast.LENGTH_SHORT).show();
        } else {

            animeTitleTv.setText(mCurrentAnime.getEpisodeList().get(indexPlayingItem).getFullName());
            toolbarTitleTv.setText(mCurrentAnime.getTitle());
//            episodeNameTv.setText(mCurrentAnime.episode.name);

            mNumberEpisodeAdapter.setCurrentNum(mCurrentAnime.getEpisodeList().get(indexPlayingItem).getName());
            mNumberEpisodeAdapter.setEpisodeList(mCurrentAnime.getEpisodeList());

            //Init player
            initializePlayer();
            if (mExoPlayerFullscreen) {
                ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
                mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mFullScreenIcon.setImageDrawable(VideoPlayerActivity.this.getResources().getDrawable(R.drawable.ic_fullscreen_skrink));
                mFullScreenDialog.show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        outState.putInt(STATE_INDEX_PLAYING_ITEM, indexPlayingItem);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
        if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null) {
            mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
//        player.stop();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(VideoPlayerActivity.this.getResources().getDrawable(R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {
        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(VideoPlayerActivity.this.getResources().getDrawable(R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {
        PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }

    private void playerMapView() {
        progressBarLayout = mExoPlayerView.findViewById(R.id.progress_bar_layout);
        errorMsgPlayerTv = mExoPlayerView.findViewById(R.id.error_player_message_tv);
        coverPlayerIv = mExoPlayerView.findViewById(R.id.player_cover_iv);
    }

    private void initCoverImage() {
//        Picasso.with(VideoPlayerActivity.this)
//                .load(mCurrentAnime.coverImage)
//                .resize(750, 400)
//                .into(coverPlayerIv);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);

        Glide.with(VideoPlayerActivity.this)
                .load(mCurrentAnime.getCoverImage())
                .thumbnail(0.1f)
                .apply(requestOptions)
                .into(coverPlayerIv);
    }

    private void showErrorMessage(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            errorMsgPlayerTv.setText(msg);
        } else {
            errorMsgPlayerTv.setText(R.string.default_msg_player_error);
        }
    }

    private void hideErrorMessage() {
        errorMsgPlayerTv.setVisibility(View.GONE);
    }

    private void showProgressLayout(boolean isShowCover) {
        if(isShowCover){
            coverPlayerIv.setVisibility(View.VISIBLE);
        }
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressLayout() {
        progressBarLayout.setVisibility(View.GONE);
        coverPlayerIv.setVisibility(View.GONE);
    }

    private void initializePlayer() {
        if (player == null) {
            playerMapView();
            showProgressLayout(true);
            initCoverImage();
            initFullscreenDialog();
            initFullscreenButton();

            Log.i(TAG, "initializePlayer");
            userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
            mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"),
                    (TransferListener<? super DataSource>) BANDWIDTH_METER);

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            player.addListener(this);
            mExoPlayerView.setPlayer(player);

            prepareContentPlayer(mCurrentAnime.getEpisodeList().get(indexPlayingItem));

        }
        if (mExoPlayerFullscreen) {
            ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
            mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(VideoPlayerActivity.this.getResources().getDrawable(R.drawable.ic_fullscreen_skrink));
            mFullScreenDialog.show();
        }
    }

    private void prepareContentPlayer(Episode episode) {
        coverPlayerIv.setVisibility(View.VISIBLE);
        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        mediaSource = buildMediaSource(Uri.parse(episode.getDirectUrl()), null);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        if (haveResumePosition) {
            if (mResumePosition <= 1000) {
                mResumePosition = 0;
            } else {
                mResumePosition -= 1000;
            }
            mExoPlayerView.getPlayer().seekTo(mResumePosition);
        }
    }

    public void pauseVideo() {
        if (player != null) {
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
            }
        }
    }

    public int resumeVideo() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        return 0;
    }

    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
//                return new ExtractorMediaSource(uri,
//                        mediaDataSourceFactory, new DefaultExtractorsFactory(), null, null);
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
        return factory;
    }


    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    @Override
    public void onItemClick(Episode item, int position) {
        coverPlayerIv.setVisibility(View.VISIBLE);
        showProgressLayout(false);
        indexPlayingItem = position;
        pauseVideo();

        mExoPlayerView.clearFocus();

        if (!TextUtils.isEmpty(item.getDirectUrl())) {
            prepareContentPlayer(item);
        } else {
            webViewClient.setRunGetSourceWeb(true);
            webView.loadUrl(item.getUrl());
        }
        mNumberEpisodeAdapter.setCurrentNum(item.getName());
        mNumberEpisodeAdapter.notifyDataSetChanged();
        Toast.makeText(this, mCurrentAnime.getTitle() + " - Episode: " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading) {
            showProgressLayout(false);
        } else {
            hideProgressLayout();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_IDLE:
                showProgressLayout(true);
                break;
            case Player.STATE_BUFFERING:
                hideErrorMessage();
                showProgressLayout(false);
            case Player.STATE_READY:
                mExoPlayerView.requestFocus();
                hideProgressLayout();
                hideErrorMessage();
            case Player.STATE_ENDED:
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        showErrorMessage(error.getMessage());
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back_btn:
                VideoPlayerActivity.this.finish();
                break;
        }
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.decoderName);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
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
                try {
                    String html = URLDecoder.decode(url, "UTF-8").substring(9);

                    Document playerDocument = Jsoup.parse(html);
                    if (playerDocument != null) {

                        Anime result = new AnimeParser().getDirectLinkPlayer(playerDocument, webView, mCurrentAnime, indexPlayingItem);
                        if (result != null) {
                            mCurrentAnime = result;
                        } else {
                            return true;
                        }
                        //If we have direct link -> call player prepare content
                        Episode ep = mCurrentAnime.getEpisodeList().get(indexPlayingItem);
                        if (!TextUtils.isEmpty(ep.getDirectUrl())) {

                            //If indexFavorite != -1 -> update cache favorite data
                            int indexFavoriteItem = AnimeDataManager.getInstance().getIndexFavoriteItem();
                            if (indexFavoriteItem > 0) {
                                //Change cache favorite data to current anime data
                                List<Anime> favoriteAnimeList = AnimeDataManager.getInstance().getFavoriteAnimeList();
                                favoriteAnimeList.set(indexFavoriteItem, mCurrentAnime);
                                //Save data
                                PreferenceHelper.getInstance(VideoPlayerActivity.this)
                                        .saveListFavoriteAnime(favoriteAnimeList);
                                Log.i(TAG, "hello");
                            }

                            webView.stopLoading();
                            Episode episode = ep;
                            animeTitleTv.setText(ep.getFullName());
                            prepareContentPlayer(episode);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                    showErrorMessage(e.getMessage());
                    Toast.makeText(VideoPlayerActivity.this, "[" + TAG + "] - " + "Can not get link episode", Toast.LENGTH_LONG).show();
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


}
