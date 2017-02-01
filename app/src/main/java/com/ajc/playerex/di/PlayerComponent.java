package com.ajc.playerex.di;

import com.ajc.playerex.activities.AudioActivity;
import com.ajc.playerex.activities.SecondAudioActivity;
import com.ajc.playerex.activities.VideoListActivity;
import com.ajc.playerex.activities.VideoPlayerActivity;
import com.ajc.playerex.main.MainActivity;

import javax.inject.Singleton;

import dagger.Component;
import es.lombrinus.projects.mods.chromecastlib.di.ChromecastModule;
import es.lombrinus.projects.mods.notificationlib.module.PlayerNotificationModule;
import es.lombrinus.projects.mods.playercore.audioplayer.di.PlayerModule;

/**
 * Created by i20206 on 26/11/2015.
 */
@Singleton
@Component(modules = {PlayerModule.class, PlayerNotificationModule.class, ChromecastModule.class})
public interface PlayerComponent {

    void inject(AudioActivity activity);

    void inject(SecondAudioActivity activity);

    void inject(VideoPlayerActivity activity);

    void inject(VideoListActivity activity);

    void inject(MainActivity activity);
}
