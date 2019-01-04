package com.guichaguri.trackplayer.service;

import android.os.Binder;
import android.os.Bundle;
import com.facebook.react.bridge.Promise;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.IcyStreamUpdate;

/**
 * @author Guichaguri
 */
public class MusicBinder extends Binder {

    private final MusicService service;
    private final MusicManager manager;

    public MusicBinder(MusicService service, MusicManager manager) {
        this.service = service;
        this.manager = manager;
        
        IcyStreamUpdate.setService(service);
    }

    public void post(Runnable r) {
        service.handler.post(r);
    }

    public MusicManager getManager() {
        return manager;
    }

    public ExoPlayback getPlayback() {
        ExoPlayback playback = manager.getPlayback();

        // TODO remove?
        if(playback == null) {
            playback = manager.createLocalPlayback(new Bundle());
            manager.switchPlayback(playback);
        }

        return playback;
    }

    public void setupPlayer(Bundle bundle, Promise promise) {
        manager.switchPlayback(manager.createLocalPlayback(bundle));
        promise.resolve(null);
    }

    public void updateOptions(Bundle bundle) {
        manager.setStopWithApp(bundle.getBoolean("stopWithApp", false));
        manager.getMetadata().updateOptions(bundle);
    }

    public int getRatingType() {
        return manager.getMetadata().getRatingType();
    }
    
    public void updateMetadata(Track track) {
        manager.updateMetadata(track);
    }

    public void destroy() {
        service.destroy();
        service.stopSelf();
    }

}
