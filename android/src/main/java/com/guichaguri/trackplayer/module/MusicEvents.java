package com.guichaguri.trackplayer.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import android.media.AudioManager;
import android.bluetooth.BluetoothHeadset;
import com.guichaguri.trackplayer.service.Utils;

/**
 * @author Guichaguri
 */
public class MusicEvents extends BroadcastReceiver {

    // Media Control Events
    public static final String BUTTON_PLAY = "remote-play";
    public static final String BUTTON_PLAY_FROM_ID = "remote-play-id";
    public static final String BUTTON_PLAY_FROM_SEARCH = "remote-play-search";
    public static final String BUTTON_PAUSE = "remote-pause";
    public static final String BUTTON_STOP = "remote-stop";
    public static final String BUTTON_SKIP = "remote-skip";
    public static final String BUTTON_SKIP_NEXT = "remote-next";
    public static final String BUTTON_SKIP_PREVIOUS = "remote-previous";
    public static final String BUTTON_SEEK_TO = "remote-seek";
    public static final String BUTTON_SET_RATING = "remote-set-rating";
    public static final String BUTTON_JUMP_FORWARD = "remote-jump-forward";
    public static final String BUTTON_JUMP_BACKWARD = "remote-jump-backward";
    public static final String BUTTON_DUCK = "remote-duck";

    // Playback Events
    public static final String PLAYBACK_STATE = "playback-state";
    public static final String PLAYBACK_TRACK_CHANGED = "playback-track-changed";
    public static final String PLAYBACK_QUEUE_ENDED = "playback-queue-ended";
    public static final String PLAYBACK_METADATA = "playback-metadata-received";
    public static final String PLAYBACK_ERROR = "playback-error";

    // Audio Events
    public static final String HEADSET_PLUGGED_IN = "headset-plugged-in";
    public static final String HEADSET_PLUGGED_OUT = "headset-plugged-out";

    public static final String BLUETOOTH_CONNECTED = "bluetooth-connected";
    public static final String BLUETOOTH_DISCONNECTED = "bluetooth-disconnected";

    private final ReactContext reactContext;

    public MusicEvents(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String event = intent.getStringExtra("event");
        Bundle data = intent.getBundleExtra("data");

        WritableMap map = data != null ? Arguments.fromBundle(data) : null;

        reactContext.getJSModule(RCTDeviceEventEmitter.class).emit(event, map);

    }

}
