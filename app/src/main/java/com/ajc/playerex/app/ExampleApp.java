package com.ajc.playerex.app;

import android.support.multidex.MultiDexApplication;

import com.ajc.playerex.di.DaggerPlayerComponent;
import com.ajc.playerex.di.PlayerComponent;

import es.lombrinus.projects.mods.chromecastlib.di.ChromecastModule;
import es.lombrinus.projects.mods.notificationlib.module.PlayerNotificationModule;
import es.lombrinus.projects.mods.playercore.audioplayer.di.PlayerModule;
import es.lombrinus.projects.mods.playercore.audioplayer.model.ContentType;

/**
 * Created by antonio.hormigo on 25/11/2015.
 */
public class ExampleApp extends MultiDexApplication {

    PlayerComponent componentAudio;
    PlayerComponent componentVideo;

    @Override
    public void onCreate() {

        super.onCreate();

        componentAudio = DaggerPlayerComponent.builder()
                .playerModule(new PlayerModule(this, ContentType.AUDIO))
                .playerNotificationModule(new PlayerNotificationModule(this))
                .build();

        componentVideo = DaggerPlayerComponent.builder()
                .playerModule(new PlayerModule(this, ContentType.VIDEO))
                .playerNotificationModule(new PlayerNotificationModule(this))
                .chromecastModule(new ChromecastModule())
                .build();
    }

    public PlayerComponent getComponentAudio() {
        return componentAudio;
    }

    public PlayerComponent getComponentVideo() {
        return componentVideo;
    }
}
