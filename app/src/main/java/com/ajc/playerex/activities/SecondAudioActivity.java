package com.ajc.playerex.activities;

import android.app.Notification;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.ajc.playerex.R;
import com.ajc.playerex.app.ExampleApp;
import com.ajc.playerex.listeners.PlayerStatsManager;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import es.lombrinus.projects.mods.notificationlib.impl.NotificationPlayerManager;
import es.lombrinus.projects.mods.notificationlib.interfaces.AJCNotification;
import es.lombrinus.projects.mods.notificationlib.interfaces.NotificationCallback;
import es.lombrinus.projects.mods.notificationlib.model.PlayerNotificationConfig;
import es.lombrinus.projects.mods.playercore.audioplayer.model.Asset;
import es.lombrinus.projects.mods.playercore.audioplayer.model.AudioPlayerView;
import es.lombrinus.projects.mods.playercore.audioplayer.model.CList;
import es.lombrinus.projects.mods.playercore.audioplayer.model.ContentType;
import es.lombrinus.projects.mods.playercore.audioplayer.model.Controls;
import es.lombrinus.projects.mods.playercore.audioplayer.player.AJCPlayer;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.PlayerControlBarManager;
import es.lombrinus.projects.mods.playercore.audioplayer.view.LoadingView;

/**
 * Created by antoniohormigo on 20/1/16.
 */
public class SecondAudioActivity extends AppCompatActivity implements LoadingView, NotificationCallback {

    private static final String URL_AUDIO = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";
    private static final String URL_AUDIO_MP3 = "https://archive.org/download/testmp3testfile/mpthreetest.mp3";

    @Inject
    AJCPlayer audioPlayer;
    @Inject
    AJCNotification audioNotification;

    private String mImageNotification;
    private EditText mTitle;
    private EditText mSubtitle;
    private CheckBox mStop;
    private CheckBox mPlayPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_second);

        ((ExampleApp) getApplication()).getComponentAudio().inject(this);

        final Button play = (Button) findViewById(R.id.button);
        final Button pause = (Button) findViewById(R.id.button2);
        final Button stop = (Button) findViewById(R.id.button3);
        final Spinner spinner = (Spinner) findViewById(R.id.spinnerVideos);

        String[] arraySpinner = new String[] {
                "m3u8", "mp3"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        spinner.setAdapter(adapter);

        mTitle = (EditText) findViewById(R.id.textViewTitle);
        mSubtitle = (EditText) findViewById(R.id.textViewSubTitle);
        mStop = (CheckBox) findViewById(R.id.checkboxStop);
        mPlayPause = (CheckBox) findViewById(R.id.checkboxPlayPause);

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView actual = (TextView) findViewById(R.id.actualPosition);
        final TextView duration = (TextView) findViewById(R.id.duration);

        CList plays = new CList(play);
        CList pauses = new CList(pause);
        CList stops = new CList(stop);

        AudioPlayerView playerView = new AudioPlayerView(actual, duration, seekBar);
        audioPlayer.addEventListener(new PlayerControlBarManager(this, new Controls(plays, pauses, stops), this, playerView));

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.audio_player_notification_little);
        RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.audio_player_notification);

        mImageNotification = "http://memowords.lombrinus.com/gallery/sound.png";

       PlayerNotificationConfig notifConfig = new PlayerNotificationConfig(
                R.mipmap.ic_audio_small_icon, contentView, bigContentView,
                R.id.play_icon, R.id.pause_icon, R.id.stop_icon, R.id.progressbar_player,
               false, false, "prueba de notificacion 2 | Ticker", null);

        audioPlayer.addEventListener(new NotificationPlayerManager(notifConfig, audioNotification, this));

        PlayerStatsManager playerStatsManager = new PlayerStatsManager();
        audioPlayer.addEventListener(playerStatsManager);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = String.valueOf(spinner.getSelectedItemId());

                if (spinner.getSelectedItemPosition() == 0) {
                    // m3u8
                    audioPlayer.play(new Asset(id, URL_AUDIO, ContentType.AUDIO), true);
                } else {
                    // mp3
                    audioPlayer.play(new Asset(id, URL_AUDIO_MP3, ContentType.AUDIO), true);
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.pause();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.release();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        final TextView load = (TextView) findViewById(R.id.textView);
        load.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        final TextView load = (TextView) findViewById(R.id.textView);
        load.setVisibility(View.INVISIBLE);
    }

    @Override
    public RemoteViews notificationChange(Notification notification, int notificationId, RemoteViews remoteViews) {

        if (mImageNotification != null) {
            remoteViews.setViewVisibility(R.id.notification_icon, View.VISIBLE);
            Picasso.with(this).load(mImageNotification)
                    .into(remoteViews, R.id.notification_icon, notificationId, notification);
        }

        remoteViews.setViewVisibility(R.id.notification_title, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notification_title, mTitle.getText().toString());

        remoteViews.setViewVisibility(R.id.notification_subtitle, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notification_subtitle, mSubtitle.getText().toString());

        if (!mStop.isChecked()) {
            remoteViews.setViewVisibility(R.id.stop_icon, View.GONE);
        }

        if (!mPlayPause.isChecked()) {
            remoteViews.setViewVisibility(R.id.play_icon, View.GONE);
            remoteViews.setViewVisibility(R.id.pause_icon, View.GONE);
        }

        return remoteViews;
    }

    @Override
    public void notificationViewClicked(int id) {
        if (R.id.play_icon == id) {
            // Actions you want to do
        }
    }
}
