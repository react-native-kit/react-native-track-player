package com.guichaguri.trackplayer.module;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.facebook.react.bridge.*;
import com.google.android.exoplayer2.C;
import com.guichaguri.trackplayer.service.MusicBinder;
import com.guichaguri.trackplayer.service.MusicService;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import android.media.AudioManager;
import android.bluetooth.BluetoothHeadset;

/**
 * @author Guichaguri
 */
public class MusicModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private MusicBinder binder;
    private MusicEvents eventHandler;
    private ArrayDeque<Runnable> initCallbacks = new ArrayDeque<>();
    private boolean connecting = false;
    private Bundle options;

    public MusicModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @Nonnull
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void initialize() {
        ReactContext context = getReactApplicationContext();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(Utils.EVENT_INTENT);

        eventHandler = new MusicEvents(context);
        manager.registerReceiver(eventHandler, filter);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        ReactContext context = getReactApplicationContext();

        if(eventHandler != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);

            manager.unregisterReceiver(eventHandler);
            eventHandler = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized(this) {
            binder = (MusicBinder) service;
            connecting = false;

        // Reapply options that user set before with updateOptions
            if (options != null) {
                binder.updateOptions(options);
            }

            // Triggers all callbacks
            while (!initCallbacks.isEmpty()) {
                binder.post(initCallbacks.remove());
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        synchronized(this) {
            binder = null;
            connecting = false;
        }
    }

    private boolean isBinderReady() {
        return binder != null && binder.isReadyForPost();
    }

    /**
     * Waits for a connection to the service and/or runs the {@link Runnable} in the player thread
     */
    private void waitForConnection(Runnable r) {
        if(isBinderReady()) {
            binder.post(r);
            return;
        } else {
            initCallbacks.add(r);
        }

        if(connecting) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Binds the service to get a MediaWrapper instance
        Intent intent = new Intent(context, MusicService.class);
        ContextCompat.startForegroundService(context, intent);
        intent.setAction(Utils.CONNECT_INTENT);
        context.bindService(intent, this, 0);

        connecting = true;
    }

    private void runOnConnectionOrReject(final Promise callback, Runnable r) {
        if(isBinderReady()) {
            binder.post(r);
        } else {
            callback.reject("playback", "The playback is not initialized");
        }
    }

    /* ****************************** API ****************************** */

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();

        // Capabilities
        constants.put("CAPABILITY_PLAY", PlaybackStateCompat.ACTION_PLAY);
        constants.put("CAPABILITY_PLAY_FROM_ID", PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        constants.put("CAPABILITY_PLAY_FROM_SEARCH", PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
        constants.put("CAPABILITY_PAUSE", PlaybackStateCompat.ACTION_PAUSE);
        constants.put("CAPABILITY_STOP", PlaybackStateCompat.ACTION_STOP);
        constants.put("CAPABILITY_SEEK_TO", PlaybackStateCompat.ACTION_SEEK_TO);
        constants.put("CAPABILITY_SKIP", PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM);
        constants.put("CAPABILITY_SKIP_TO_NEXT", PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        constants.put("CAPABILITY_SKIP_TO_PREVIOUS", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        constants.put("CAPABILITY_SET_RATING", PlaybackStateCompat.ACTION_SET_RATING);
        constants.put("CAPABILITY_JUMP_FORWARD", PlaybackStateCompat.ACTION_FAST_FORWARD);
        constants.put("CAPABILITY_JUMP_BACKWARD", PlaybackStateCompat.ACTION_REWIND);

        // States
        constants.put("STATE_NONE", PlaybackStateCompat.STATE_NONE);
        constants.put("STATE_READY", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_PLAYING", PlaybackStateCompat.STATE_PLAYING);
        constants.put("STATE_PAUSED", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_STOPPED", PlaybackStateCompat.STATE_STOPPED);
        constants.put("STATE_BUFFERING", PlaybackStateCompat.STATE_BUFFERING);
        constants.put("STATE_CONNECTING", PlaybackStateCompat.STATE_CONNECTING);

        // Rating Types
        constants.put("RATING_HEART", RatingCompat.RATING_HEART);
        constants.put("RATING_THUMBS_UP_DOWN", RatingCompat.RATING_THUMB_UP_DOWN);
        constants.put("RATING_3_STARS", RatingCompat.RATING_3_STARS);
        constants.put("RATING_4_STARS", RatingCompat.RATING_4_STARS);
        constants.put("RATING_5_STARS", RatingCompat.RATING_5_STARS);
        constants.put("RATING_PERCENTAGE", RatingCompat.RATING_PERCENTAGE);

        return constants;
    }

    @ReactMethod
    public synchronized void setupPlayer(ReadableMap data, final Promise promise) {
        final Bundle options = Arguments.toBundle(data);

        waitForConnection(() -> binder.setupPlayer(options, promise));
    }

    @ReactMethod
    public void isServiceRunning(final Promise promise) {
        promise.resolve(isBinderReady());
    }

    @ReactMethod
    public synchronized void destroy() {
        try {
            synchronized(this) {
                if(binder != null) {
                    binder.destroy();
                    binder = null;
                }
            }

            ReactContext context = getReactApplicationContext();
            if(context != null) context.unbindService(this);
        } catch(Exception ex) {
            // This method shouldn't be throwing unhandled errors even if something goes wrong.
            Log.e(Utils.LOG, "An error occurred while destroying the service", ex);
        }
    }

    @ReactMethod
    public synchronized void updateOptions(ReadableMap data, final Promise callback) {
        // keep options as we may need them for correct MetadataManager reinitialization later
        options = Arguments.toBundle(data);

        runOnConnectionOrReject(callback, () -> {
            binder.updateOptions(options);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void add(ReadableArray tracks, final String insertBeforeId, final Promise callback) {
        final ArrayList bundleList = Arguments.toList(tracks);

        runOnConnectionOrReject(callback, () -> {
            List<Track> trackList;

            try {
                trackList = Track.createTracks(getReactApplicationContext(), bundleList, binder.getRatingType());
            } catch(Exception ex) {
                callback.reject("invalid_track_object", ex);
                return;
            }

            List<Track> queue = binder.getPlayback().getQueue();
            int index = -1;

            if(insertBeforeId != null) {
                for(int i = 0; i < queue.size(); i++) {
                    if(queue.get(i).id.equals(insertBeforeId)) {
                        index = i;
                        break;
                    }
                }
            } else {
                index = queue.size();
            }

            if(index == -1) {
                callback.reject("track_not_in_queue", "Given track ID was not found in queue");
            } else if(trackList == null || trackList.isEmpty()) {
                callback.reject("invalid_track_object", "Track is missing a required key");
            } else if(trackList.size() == 1) {
                binder.getPlayback().add(trackList.get(0), index, callback);
            } else {
                binder.getPlayback().add(trackList, index, callback);
            }
        });
    }

    @ReactMethod
    public synchronized void remove(ReadableArray tracks, final Promise callback) {
        final ArrayList trackList = Arguments.toList(tracks);

        runOnConnectionOrReject(callback, () -> {
            List<Track> queue = binder.getPlayback().getQueue();
            List<Integer> indexes = new ArrayList<>();

            for(Object o : trackList) {
                String id = o.toString();

                for(int i = 0; i < queue.size(); i++) {
                    if(queue.get(i).id.equals(id)) {
                        indexes.add(i);
                        break;
                    }
                }
            }

            if (!indexes.isEmpty()) {
                binder.getPlayback().remove(indexes, callback);
            } else {
                callback.resolve(null);
            }
        });
    }

    @ReactMethod
    public synchronized void updateMetadataForTrack(String id, ReadableMap map, final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            ExoPlayback playback = binder.getPlayback();
            List<Track> queue = playback.getQueue();
            Track track = null;
            int index = -1;

            for(int i = 0; i < queue.size(); i++) {
                track = queue.get(i);

                if(track.id.equals(id)) {
                    index = i;
                    break;
                }
            }

            if(index == -1) {
                callback.reject("track_not_in_queue", "No track found");
            } else {
                track.setMetadata(getReactApplicationContext(), Arguments.toBundle(map), binder.getRatingType());
                playback.updateTrack(index, track);
                callback.resolve(null);
            }
        });
    }

    @ReactMethod
    public synchronized void removeUpcomingTracks(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().removeUpcomingTracks();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void skip(final String track, final Promise callback) {
        runOnConnectionOrReject(callback, () -> binder.getPlayback().skip(track, callback));
    }

    @ReactMethod
    public synchronized void skipToNext(final Promise callback) {
        runOnConnectionOrReject(callback, () -> binder.getPlayback().skipToNext(callback));
    }

    @ReactMethod
    public synchronized void skipToPrevious(final Promise callback) {
        runOnConnectionOrReject(callback, () -> binder.getPlayback().skipToPrevious(callback));
    }

    @ReactMethod
    public synchronized void reset(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().reset();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void play(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().play();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void playWithEarPiece(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().playWithEarPiece();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void pause(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().pause();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void stop(final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().stop();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void seekTo(final float seconds, final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            long secondsToSkip = Utils.toMillis(seconds);
            binder.getPlayback().seekTo(secondsToSkip);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void setVolume(final float volume, final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().setVolume(volume);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void getVolume(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> callback.resolve(binder.getPlayback().getVolume()));
        } else {
            callback.resolve(null);
        }
    }

    @ReactMethod
    public synchronized void setRate(final float rate, final Promise callback) {
        runOnConnectionOrReject(callback, () -> {
            binder.getPlayback().setRate(rate);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public synchronized void getRate(final Promise callback) {

        if(isBinderReady()) {
            binder.post(() -> callback.resolve(binder.getPlayback().getRate()));
        } else {
            callback.resolve(null);
        }
    }

    @ReactMethod
    public synchronized void getTrack(final String id, final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                List<Track> tracks = binder.getPlayback().getQueue();
                for(Track track : tracks) {
                    if(track.id.equals(id)) {
                        callback.resolve(Arguments.fromBundle(track.originalItem));
                        return;
                    }
                }
                callback.resolve(null);
            });
        } else {
            callback.resolve(null);
        }
    }

    @ReactMethod
    public synchronized void getQueue(Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                List queue = new ArrayList();
                List<Track> tracks = binder.getPlayback().getQueue();
                for(Track track : tracks) {
                    queue.add(track.originalItem);
                }
                callback.resolve(Arguments.fromList(queue));
            });
        } else {
            callback.resolve(new ArrayList());
        }
    }

    @ReactMethod
    public synchronized void getCurrentTrack(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                Track track = binder.getPlayback().getCurrentTrack();
                if(track == null) {
                    callback.resolve(null);
                } else {
                    callback.resolve(track.id);
                }
            });
        } else {
            callback.resolve(null);
        }
    }

    @ReactMethod
    public synchronized void getDuration(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                long duration = binder.getPlayback().getDuration();
                if(duration == C.TIME_UNSET) {
                    callback.resolve(Utils.toSeconds(0));
                } else {
                    callback.resolve(Utils.toSeconds(duration));
                }
            });
        } else {
            callback.resolve(Utils.toSeconds(0));
        }
    }

    @ReactMethod
    public synchronized void getBufferedPosition(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                long position = binder.getPlayback().getBufferedPosition();
                if(position == C.POSITION_UNSET) {
                    callback.resolve(Utils.toSeconds(0));
                } else {
                    callback.resolve(Utils.toSeconds(position));
                }
            });
        } else {
            callback.resolve(Utils.toSeconds(0));
        }
    }

    @ReactMethod
    public synchronized void getPosition(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> {
                long position = binder.getPlayback().getPosition();
                if(position == C.POSITION_UNSET) {
                    callback.reject("unknown", "Unknown position");
                } else {
                    callback.resolve(Utils.toSeconds(position));
                }
            });
        } else {
            callback.reject("unknown", "Unknown position");
        }
    }

    @ReactMethod
    public synchronized void getState(final Promise callback) {
        if(isBinderReady()) {
            binder.post(() -> callback.resolve(binder.getPlayback().getState()));
        } else {
            callback.resolve(PlaybackStateCompat.STATE_NONE);
        }
    }
}
