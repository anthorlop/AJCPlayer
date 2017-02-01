package com.ajc.playerex.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ajc.playerex.R;
import com.ajc.playerex.app.ExampleApp;
import com.ajc.playerex.listeners.PlayerStatsManager;

import javax.inject.Inject;

import es.lombrinus.projects.mods.playercore.audioplayer.model.Asset;
import es.lombrinus.projects.mods.playercore.audioplayer.model.CList;
import es.lombrinus.projects.mods.playercore.audioplayer.model.ContentType;
import es.lombrinus.projects.mods.playercore.audioplayer.model.Controls;
import es.lombrinus.projects.mods.playercore.audioplayer.model.PlaybackSettings;
import es.lombrinus.projects.mods.playercore.audioplayer.model.VideoPlayerOptions;
import es.lombrinus.projects.mods.playercore.audioplayer.model.VideoPlayerView;
import es.lombrinus.projects.mods.playercore.audioplayer.player.AJCPlayer;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.OnDoubleClick;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.VideoControlBarManager;
import es.lombrinus.projects.mods.playercore.audioplayer.view.LoadingView;

public class VideoListActivity extends AppCompatActivity implements LoadingView, OnDoubleClick {

    @Inject
    AJCPlayer videoPlayer;

    private SurfaceView mSurfaceView;
    private FrameLayout mFrameLayout;

    private SurfaceHolder holder;

    private SurfaceHolder holder1;
    private SurfaceHolder holder2;
    private SurfaceHolder holder3;
    private View actualView;

    private Boolean autoPlay;
    private String idVideo;
    private String urlVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        ((ExampleApp) getApplication()).getComponentVideo().inject(this);

        ((TextView) findViewById(R.id.emb1).findViewById(R.id.idVideoTxt)).setText("MP4");
        ((TextView) findViewById(R.id.emb2).findViewById(R.id.idVideoTxt)).setText("HLS");
        ((TextView) findViewById(R.id.emb3).findViewById(R.id.idVideoTxt)).setText("DASH");

        findViewById(R.id.emb1).findViewById(R.id.idVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPlay = true;
                initViews(R.id.emb1);
            }
        });
        findViewById(R.id.emb2).findViewById(R.id.idVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPlay = true;
                initViews(R.id.emb2);
            }
        });
        findViewById(R.id.emb3).findViewById(R.id.idVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPlay = true;
                initViews(R.id.emb3);
            }
        });

        findViewById(R.id.buttonFullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFullscreen();
            }
        });
    }

    private void initViews(int emb) {

        if (actualView == null
                || actualView.getId() != emb) {

            String lastVideoId = null;
            if (actualView != null) {
                actualView.findViewById(R.id.idVideo).setVisibility(View.VISIBLE);
                actualView.findViewById(R.id.idVideo).getLayoutParams().height = mFrameLayout.getHeight();
                lastVideoId = String.valueOf(actualView.getId());
            }

            actualView = findViewById(emb);

            idVideo = ((TextView) actualView.findViewById(R.id.idVideoTxt)).getText().toString();

            if (idVideo.equals("MP4")) {
                urlVideo = getString(R.string.url_video_list_mp4);
            } else if (idVideo.equals("HLS")) {
                urlVideo = getString(R.string.url_video_list_hls);
            } else if (idVideo.equals("DASH")) {
                urlVideo = getString(R.string.url_video_list_mpd);
            }

            if (lastVideoId != null && !lastVideoId.equals(actualView.getId())) {
                videoPlayer.release();
            }

            actualView.findViewById(R.id.audioPlayer).setVisibility(View.VISIBLE);

            mSurfaceView = (SurfaceView) actualView.findViewById(R.id.videoSurface);
            mFrameLayout = (FrameLayout) actualView.findViewById(R.id.videoSurfaceContainer);

            actualView.findViewById(R.id.idVideo).setVisibility(View.GONE);
            initSurface(emb);
        }
    }

    private void goToFullscreen() {
        Intent intent = new Intent(VideoListActivity.this, VideoPlayerActivity.class);
        Bundle extras = new Bundle();
        extras.putString(VideoPlayerActivity.VIDEO_ID, idVideo);
        extras.putString(VideoPlayerActivity.VIDEO_PATH, urlVideo);
        extras.putBoolean(VideoPlayerActivity.FULLSCREEN_ID, true);
        intent.putExtras(extras);
        reload = true;
        VideoListActivity.this.startActivity(intent);
    }

    private void initSurface(int emb) {
        if (emb == R.id.emb1 && holder1 != null) {
            initPlayer(holder1);
        } else if (emb == R.id.emb2 && holder2 != null) {
            initPlayer(holder2);
        } else if (emb == R.id.emb3 && holder3 != null) {
            initPlayer(holder3);
        } else {
            holder = mSurfaceView.getHolder();
            DisplayMetrics metrics = new DisplayMetrics();
            VideoListActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            holder.setFixedSize(metrics.widthPixels, (int) ((float) metrics.widthPixels / (float) 16 / (float) 9));
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    initPlayer(surfaceHolder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            });
        }
    }

    private boolean reload = false;

    @Override
    protected void onResume() {

//        recalculateVideoDimens();

        if (reload) {

            reload = false;

            if (actualView != null) {

                holder1 = null;
                holder2 = null;
                holder3 = null;

                initSurface(actualView.getId());
            }
        }

        super.onResume();
    }

    private void initPlayer(SurfaceHolder surfaceHolder) {

        if (actualView.getId() == R.id.emb1) {
            holder1 = surfaceHolder;
        } else if (actualView.getId() == R.id.emb2) {
            holder2 = surfaceHolder;
        } else if (actualView.getId() == R.id.emb3) {
            holder3 = surfaceHolder;
        }

        final View play = actualView.findViewById(R.id.button);
        final View pause = actualView.findViewById(R.id.button2);
        final View stop = actualView.findViewById(R.id.button3);

        final SeekBar seekBar = (SeekBar) actualView.findViewById(R.id.seekBar);
        final TextView actual = (TextView) actualView.findViewById(R.id.actualPosition);
        final TextView duration = (TextView) actualView.findViewById(R.id.duration);

        CList plays = new CList(play);
        CList pauses = new CList(pause);
        CList stops = new CList(stop);

        View view = actualView.findViewById(R.id.audioPlayer);

        View fullscreen = actualView.findViewById(R.id.buttonFullscreen);

        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFullscreen();
            }
        });

        CList controller = new CList(view, fullscreen);

        VideoPlayerView videoPlayerView = new VideoPlayerView(mFrameLayout, surfaceHolder, actual, duration, seekBar, controller);
        VideoPlayerOptions options = new VideoPlayerOptions(ActivityInfo.SCREEN_ORIENTATION_SENSOR, false, false, false);
        Controls controls = new Controls(plays, pauses, stops);

        //videoPlayer.setOptions(new PlaybackSettings(...));

        videoPlayer.addEventListener(new VideoControlBarManager(VideoListActivity.this, controls, VideoListActivity.this, VideoListActivity.this, videoPlayerView, options));

        PlayerStatsManager playerStatsManager = new PlayerStatsManager();
        videoPlayer.addEventListener(playerStatsManager);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idVideo != null) {
                    videoPlayer.play(new Asset(String.valueOf(actualView.getId()), urlVideo, ContentType.VIDEO), autoPlay);
                } else {
                    Toast.makeText(VideoListActivity.this, "id video null", Toast.LENGTH_SHORT).show();
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
                videoPlayer.release();
                actualView.findViewById(R.id.idVideo).setVisibility(View.VISIBLE);
                actualView = null;
            }
        });

        if (autoPlay) {
            play.performClick();
        }
    }

    @Override
    public void showLoading() {
        final View load = actualView.findViewById(R.id.progressbar_video);
        load.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        final View load = actualView.findViewById(R.id.progressbar_video);
        load.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {

        if (isFinishing()) {
            videoPlayer.release();
        }

        super.onDestroy();
    }

    @Override
    public void onDoubleClick() {
        goToFullscreen();
    }
}
