package com.ajc.playerex.listeners;

import android.util.Log;

import es.lombrinus.projects.mods.playercore.audioplayer.model.Asset;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.PlayerControl;
import es.lombrinus.projects.mods.playercore.audioplayer.player.listeners.PlayerEventListener;

/**
 * Created by antoniohormigo on 11/1/16.
 */
public class PlayerStatsManager implements PlayerEventListener {

    public static Integer currentPosition = -1;

    public static String type = "VOD";

    @Override
    public void onPreparing(Asset asset, PlayerControl playerControl) {
        currentPosition = -1;
        //sendStat("onPreparing", asset);
    }

    @Override
    public void onPlayBegins(Asset asset, int duration) {
        currentPosition = 0;
        if (duration > 0) {
            type = "VOD";
        } else {
            type = "LIVE";
        }
        sendStat("onPlayBegins", asset);
    }

    @Override
    public void onResume(Asset asset, int currentPosition) {
        this.currentPosition = currentPosition;
        //sendStat("onResume", asset);
    }

    // TODO onRelease
    /*@Override
    public void onRelease() {
        this.currentPosition = -2;
    }*/

    @Override
    public void onCompletion(Asset asset) {
        this.currentPosition = -2;
        //sendStat("onCompletion", asset);
    }

    @Override
    public void onPause(Asset asset, int currentPosition) {
        this.currentPosition = currentPosition;
        //sendStat("onPause", asset);
    }

    @Override
    public void onForward(Asset asset, int currentPosition) {
        this.currentPosition = currentPosition;
        //sendStat("onForward", asset);
    }

    @Override
    public void onError(Asset asset, String error) {
        Log.d("ERROR", asset.getmContentType().name()
                + "_" + asset.getId() + "_" + error);
        /*StatsManage.sendEvent("ERROR", asset.getContentType().name()
                + "_" + asset.getId() + "_" + error, currentPosition);*/
    }

    @Override
    public void onConfigurationChanged() {

    }

    private void sendStat(String method, Asset asset) {
        if (asset != null) {
            Log.d("StatsLib", method + " - Position: " + currentPosition);
            Log.d("StatsLib", " Label:" + asset.getmContentType().name() + "_" + type + "_URL"
                    + " Action: " + asset.getmContentType().name()
                    + " Value:" + asset.getId());
            /*StatsManage.sendEvent(asset.getContentType().name(), asset.getContentType().name() + "_" + type + "_URL",  asset.getId());*/
        }
    }
}
