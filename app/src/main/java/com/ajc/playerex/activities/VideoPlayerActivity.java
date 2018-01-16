package com.ajc.playerex.activities;

import android.app.Notification;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.ajc.playerex.app.ExampleApp;
import com.ajc.playerex.listeners.PlayerStatsManager;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import com.ajc.playerex.R;

import es.lombrinus.projects.mods.chromecastlib.CastListener;
import es.lombrinus.projects.mods.chromecastlib.CastViewList;
import es.lombrinus.projects.mods.chromecastlib.CustomMediaRouteButton;
import es.lombrinus.projects.mods.chromecastlib.interfaces.AJCast;
import es.lombrinus.projects.mods.chromecastlib.interfaces.UrlToCastListener;
import es.lombrinus.projects.mods.notificationlib.interfaces.AJCNotification;
import es.lombrinus.projects.mods.notificationlib.interfaces.NotificationCallback;
import es.lombrinus.projects.mods.playercore.audioplayer.model.Asset;
import es.lombrinus.projects.mods.playercore.audioplayer.model.CList;
import es.lombrinus.projects.mods.playercore.audioplayer.model.ContentType;
import es.lombrinus.projects.mods.playercore.audioplayer.model.Controls;
import es.lombrinus.projects.mods.playercore.audioplayer.model.VideoPlayerOptions;
import es.lombrinus.projects.mods.playercore.audioplayer.model.VideoPlayerView;
import es.lombrinus.projects.mods.playercore.audioplayer.player.AJCPlayer;
import es.lombrinus.projects.mods.playercore.audioplayer.player.VideoPlayer;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.OnSubtitleDetect;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.PlayerControl;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.PlayerEventListener;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.SubtitleManager;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.VideoControlBarManager;
import es.lombrinus.projects.mods.playercore.audioplayer.view.LoadingView;

public class VideoPlayerActivity extends AppCompatActivity
        implements SurfaceHolder.Callback, LoadingView, OnSubtitleDetect, NotificationCallback, PlayerEventListener {

    public static final String URL_VIDEO = "http://techslides.com/demos/sample-videos/small.mp4";
    public static String VIDEO_ID = "VideoId";
    public static String AUTO_PLAY_ID = "AUTO_PLAY_ID";
    public static String FULLSCREEN_ID = "FULLSCREEN_ID";
    public static String VIDEO_URL = "VIDEO_URL_ID";
    public static String VIDEO_PATH = "VIDEO_PATH_ID";

    @Inject
    AJCPlayer videoPlayer;

    @Inject
    AJCast mChromeCast;

    private String mImageNotification;
    private SurfaceView mSurfaceView;
    private FrameLayout mFrameLayout;

    private View chromeCastView;

    private CustomMediaRouteButton mMediaRouteButton;

    private String idVideo;
    private Boolean autoPlay = true; // if new video
    private Boolean fullScreen;

    private View play;
    private View pause;
    private View stop;
    private SeekBar seekBar;
    private TextView current;
    private TextView duration;
    private VideoControlBarManager controlBarManager;

    private ImageView subtitlesBtn;
    private boolean subtitleEnabled = false;

    private String urlResolved;
    private String pathVideo;
    private String vttSubtitle;
    private boolean subtitlesChecked;

    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        subtitlesChecked = false;
        urlResolved = null;

        ((ExampleApp) getApplication()).getComponentVideo().inject(this);

        mChromeCast.build(this);

        //mChromeCast.setCastIcons();

        if (getIntent().getExtras() != null) {
            idVideo = getIntent().getExtras().getString(VIDEO_ID);
            autoPlay = getIntent().getExtras().getBoolean(AUTO_PLAY_ID, true);
            fullScreen = getIntent().getExtras().getBoolean(FULLSCREEN_ID, false);
            urlResolved = getIntent().getExtras().getString(VIDEO_URL);
            pathVideo = getIntent().getExtras().getString(VIDEO_PATH);
        }

        if (!fullScreen)
            videoPlayer = new VideoPlayer(this, new MediaPlayer());

        if (urlResolved == null) {
            if (pathVideo != null) {
                urlResolved = pathVideo;
            } else {
                urlResolved = URL_VIDEO;
            }
        }

        play = findViewById(R.id.button);
        pause = findViewById(R.id.button2);
        stop = findViewById(R.id.button3);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        current = (TextView) findViewById(R.id.actualPosition);
        duration = (TextView) findViewById(R.id.duration);

        seekBar.getProgressDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        subtitlesBtn = (ImageView) findViewById(R.id.button_subtitles);

        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurface);
        mFrameLayout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
        chromeCastView = findViewById(R.id.chromeCastView);
        mMediaRouteButton = (CustomMediaRouteButton) findViewById(R.id.media_route_btn);

        View fullscreenView = findViewById(R.id.buttonFullscreen);

        if (fullScreen) {

            if (fullscreenView instanceof ImageView) {
                ((ImageView) fullscreenView).setImageResource(R.mipmap.ic_fullscreen_off);
            }

            fullscreenView.setVisibility(View.VISIBLE);
            fullscreenView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        } else {

            fullscreenView.setVisibility(View.GONE);

        }

        initChromecast();

    }

    @Override
    protected void onDestroy() {

        if (!fullScreen) {
            videoPlayer.release();
        }

        //mChromeCast.stop();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (autoPlay) {
            currentPosition = videoPlayer.getCurrentPosition();
            videoPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mChromeCast.resume();

        SurfaceHolder holder = mSurfaceView.getHolder();
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        holder.setFixedSize(metrics.widthPixels, (int) ((float) metrics.widthPixels / (float) 16 / (float) 9));
        holder.addCallback(this);

        videoPlayer.onViewSizeChanged();
    }

    @Override
    public void showLoading() {
        View load = findViewById(R.id.progressbar_video);
        if (load != null) {
            load.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        View load = findViewById(R.id.progressbar_video);
        if (load != null) {
            load.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public RemoteViews notificationChange(Notification notification, int notificationId, RemoteViews remoteViews) {

        if (mImageNotification != null) {
            remoteViews.setViewVisibility(R.id.notification_icon, View.VISIBLE);
            Picasso.with(this).load(mImageNotification)
                    .into(remoteViews, R.id.notification_icon, notificationId, notification);
        }

        remoteViews.setViewVisibility(R.id.notification_title, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notification_title, "Video de prueba");

        remoteViews.setViewVisibility(R.id.notification_subtitle, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notification_subtitle, "Video de prueba");

        remoteViews.setViewVisibility(R.id.stop_icon, View.GONE);
        remoteViews.setViewVisibility(R.id.play_icon, View.GONE);
        remoteViews.setViewVisibility(R.id.pause_icon, View.GONE);

        return remoteViews;
    }

    @Override
    public void notificationViewClicked(int id) {
        if (R.id.play_icon == id) {
            // Actions you want to do
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        final CList plays = new CList(play);
        final CList pauses = new CList(pause);
        final CList stops = new CList(stop);

        View view = findViewById(R.id.audioPlayer);
        View viewTop = findViewById(R.id.audioPlayerTop);
        CList controller = new CList(view, viewTop);

        final VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, current, duration, seekBar, controller);
        final VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, true, true, true);
        final Controls controls = new Controls(plays, pauses, stops);
        controlBarManager = new VideoControlBarManager(this, controls, this, videoPlayerView, options);

        if (!fullScreen) {

            videoPlayer.addEventListener(new PlayerStatsManager());

            // final SubtitleManager subtitleManager = new SubtitleManager(this, "http://...vtt", this);
            // videoPlayer.addEventListener(subtitleManager);
        }

        videoPlayer.addEventListener(this);

        videoPlayer.addEventListener(controlBarManager);
        setButtonsActionClick(play, pause, stop);

        if (!fullScreen) {
            if (idVideo != null) {

                Asset asset = new Asset(idVideo, urlResolved, ContentType.VIDEO);
                asset.setLocalPath(!TextUtils.isEmpty(pathVideo));

                if (currentPosition > 0) {
                    videoPlayer.play(asset, currentPosition);
                } else {
                    videoPlayer.play(asset, autoPlay);
                }
            } else if (pathVideo != null) {
                Asset asset = new Asset("1", urlResolved, ContentType.VIDEO);
                asset.setLocalPath(!TextUtils.isEmpty(pathVideo));

                if (currentPosition > 0) {
                    videoPlayer.play(asset, currentPosition);
                } else {
                    videoPlayer.play(asset, autoPlay);
                }
            } else {
                Toast.makeText(VideoPlayerActivity.this, "no video played", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setButtonsActionClick(View play, View pause, View stop) {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idVideo != null || pathVideo != null) {
                    videoPlayer.play();
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "video null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPlayer.pause();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoPlay) {
                    videoPlayer.release();
                } else {
                    finish();
                }
            }
        });

        subtitlesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subtitleEnabled) {
                    subtitlesBtn.setImageResource(R.mipmap.ic_subtitle_off);
                    subtitleEnabled = false;
                } else {
                    subtitlesBtn.setImageResource(R.mipmap.ic_subtitle_on);
                    subtitleEnabled = true;
                }
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        videoPlayer.onViewSizeChanged();

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSubtitleDetect(String subtitle) {
        if (subtitleEnabled
                && subtitle != null && subtitle.length() > 0) {
            findViewById(R.id.subtitlesLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.subtitleText)).setText(subtitle);
        } else {
            findViewById(R.id.subtitlesLayout).setVisibility(View.GONE);
        }
    }

    @Override
    public void enableSubtitles(boolean enable, String vttSubtitle) {

        subtitlesChecked = true;

        if (enable) {
            subtitlesBtn.setImageResource(R.mipmap.ic_subtitle_on);
            subtitleEnabled = true;
            subtitlesBtn.setVisibility(View.VISIBLE);
        } else {
            subtitlesBtn.setImageResource(R.mipmap.ic_subtitle_off);
            subtitleEnabled = false;
            subtitlesBtn.setVisibility(View.GONE);
        }
    }

    private void initChromecast() {

        // vttSubtitle = "http://urbangoals.comli.com/subtitulo.vtt";

        if (idVideo != null) {

            //List<SubtitleCast> vtts = new ArrayList<>();
            //vtts.add(new SubtitleCast(1, "Spanish", vttSubtitle, "es"));

            mChromeCast.init(mMediaRouteButton, idVideo, urlResolved, "prueba video", "subtitle",
                    "http://memowords.lombrinus.com/gallery/sound.png",
                    new CastListener() {
                        @Override
                        public void getUrlToChromeCast(UrlToCastListener urlToCastListener) {

                            // url final enviada a chromecast
                            urlToCastListener.onUrlResolved(urlResolved);
                        }

                        @Override
                        public void onConnected(boolean enableCasting) {
                            if (enableCasting) {
                                chromeCastView.setVisibility(View.VISIBLE);
                                showChromeCastLoading();

                                videoPlayer.pause();

                                final CastViewList playsChromecast = new CastViewList(play);
                                final CastViewList pausesChromecast = new CastViewList(pause);
                                final CastViewList stopsChromecast = new CastViewList(stop);
                                final CastViewList tracksChooserChromecast = new CastViewList(subtitlesBtn);

                                mChromeCast.setChromecastControls(playsChromecast, pausesChromecast, stopsChromecast, tracksChooserChromecast, seekBar, duration, current, null);
                            }
                        }

                        @Override
                        public void onDisconnected() {
                            if (chromeCastView != null && chromeCastView.getVisibility() == View.VISIBLE) {
                                chromeCastView.setVisibility(View.GONE);
                                controlBarManager.reclaimSeekBarControl();
                                //videoPlayer.addEventListener(controlBarManager);
                                setButtonsActionClick(play, pause, stop);

                                if (autoPlay) {
                                    if (idVideo != null) {
                                        Asset asset = new Asset(idVideo, urlResolved, ContentType.VIDEO);
                                        videoPlayer.play(asset, mChromeCast.getCurrentPosition());
                                        //videoPlayer.seekTo(mChromeCast.getCurrentPosition());
                                    } else {
                                        Toast.makeText(VideoPlayerActivity.this, "id video null", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    videoPlayer.seekTo(mChromeCast.getCurrentPosition());
                                }
                            }
                        }

                        @Override
                        public void onStatusChanged(int mediaStatus) {
                            Log.d("VideoPlayerAct", "mediaStatus: " + mediaStatus);
                        }

                        @Override
                        public void onReadyToCast() {
                            hideChromeCastLoading();
                            View view = findViewById(R.id.subtitlesLayout);
                            if (view != null) {
                                view.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onForward(int position) {
                            // nothing to do
                        }
                    });
        } else {
            mMediaRouteButton.setVisibility(View.GONE);
        }
    }

    private void hideChromeCastLoading() {
        View load = findViewById(R.id.progressBarChromeCast);
        if (load != null) {
            load.setVisibility(View.INVISIBLE);
        }

    }

    private void showChromeCastLoading() {
        View load = findViewById(R.id.progressBarChromeCast);
        if (load != null) {
            load.setVisibility(View.VISIBLE);
        }
    }

    // Additional Actions

    @Override
    public void onPreparing(Asset asset, PlayerControl playerControl) {

    }

    @Override
    public void onPlayBegins(Asset asset, int i) {

    }

    @Override
    public void onResume(Asset asset, int i) {

    }

    @Override
    public void onCompletion(Asset asset) {

    }

    @Override
    public void onPause(Asset asset, int i) {

    }

    @Override
    public void onForward(Asset asset, int i) {

    }

    @Override
    public void onError(Asset asset, String message) {
        videoPlayer.release();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onConfigurationChanged() {

    }
}