package com.guichaguri.trackplayer.service.player;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.Timeline.Window;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.icy.IcyHeaders;
import com.google.android.exoplayer2.metadata.icy.IcyInfo;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Guichaguri
 */
public abstract class ExoPlayback<T extends Player> implements EventListener, MetadataOutput {

    protected final Context context;
    protected final MusicManager manager;
    protected final T player;

    protected List<Track> queue = Collections.synchronizedList(new ArrayList<>());

    // https://github.com/google/ExoPlayer/issues/2728
    protected int lastKnownWindow = C.INDEX_UNSET;
    protected long lastKnownPosition = C.POSITION_UNSET;
    protected int previousState = PlaybackStateCompat.STATE_NONE;

    public ExoPlayback(Context context, MusicManager manager, T player) {
        this.context = context;
        this.manager = manager;
        this.player = player;

        Player.MetadataComponent component = player.getMetadataComponent();
        if(component != null) component.addMetadataOutput(this);
    }

    public void initialize() {
        player.addListener(this);
    }

    public List<Track> getQueue() {
        return queue;
    }

    public abstract void add(Track track, int index, Promise promise);

    public abstract void add(Collection<Track> tracks, int index, Promise promise);

    public abstract void remove(List<Integer> indexes, Promise promise);

    public abstract void removeUpcomingTracks();

    public void updateTrack(int index, Track track) {
        int currentIndex = player.getCurrentWindowIndex();

        queue.set(index, track);

        if(currentIndex == index)
            manager.getMetadata().updateMetadata(track);
    }

    public Track getCurrentTrack() {
        int index = player.getCurrentWindowIndex();
        return index == C.INDEX_UNSET || index < 0 || index >= queue.size() ? null : queue.get(index);
    }

    public void skip(String id, Promise promise) {
        if(id == null || id.isEmpty()) {
            promise.reject("invalid_id", "The ID can't be null or empty");
            return;
        }

        for(int i = 0; i < queue.size(); i++) {
            if(id.equals(queue.get(i).id)) {
                lastKnownWindow = player.getCurrentWindowIndex();
                lastKnownPosition = player.getCurrentPosition();

                player.seekToDefaultPosition(i);
                promise.resolve(null);
                return;
            }
        }

        promise.reject("track_not_in_queue", "Given track ID was not found in queue");
    }

    public void skipToPrevious(Promise promise) {
        int prev = player.getPreviousWindowIndex();

        if(prev == C.INDEX_UNSET) {
            promise.reject("no_previous_track", "There is no previous track");
            return;
        }

        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();

        player.seekToDefaultPosition(prev);
        promise.resolve(null);
    }

    public void skipToNext(Promise promise) {
        int next = player.getNextWindowIndex();

        if(next == C.INDEX_UNSET) {
            promise.reject("queue_exhausted", "There is no tracks left to play");
            return;
        }

        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();

        player.seekToDefaultPosition(next);
        promise.resolve(null);
    }

    public void play() {
        player.setPlayWhenReady(true);
    }

    public void pause() {
        player.setPlayWhenReady(false);
    }

    public void stop() {
        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();

        player.stop(false);
        player.setPlayWhenReady(false);
        player.seekTo(0,0);
    }

    public void reset() {
        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();

        player.stop(true);
        player.setPlayWhenReady(false);
    }

    public boolean isRemote() {
        return false;
    }

    public long getPosition() {
        return player.getCurrentPosition();
    }

    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    public long getDuration() {
        return player.getDuration();
    }

    public void seekTo(long time) {
        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();

        player.seekTo(time);
    }

    public abstract float getVolume();

    public abstract void setVolume(float volume);

    public float getRate() {
        return player.getPlaybackParameters().speed;
    }

    public void setRate(float rate) {
        player.setPlaybackParameters(new PlaybackParameters(rate, player.getPlaybackParameters().pitch));
    }

    public int getState() {
        switch(player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case Player.STATE_ENDED:
                return PlaybackStateCompat.STATE_STOPPED;
            case Player.STATE_IDLE:
                return PlaybackStateCompat.STATE_NONE;
            case Player.STATE_READY:
                return player.getPlayWhenReady() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        }
        return PlaybackStateCompat.STATE_NONE;
    }

    public void destroy() {
        player.release();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        Log.d(Utils.LOG, "onTimelineChanged: " + reason);

        if((reason == Player.TIMELINE_CHANGE_REASON_PREPARED || reason == Player.TIMELINE_CHANGE_REASON_DYNAMIC) && !timeline.isEmpty()) {
            onPositionDiscontinuity(Player.DISCONTINUITY_REASON_INTERNAL);
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.d(Utils.LOG, "onPositionDiscontinuity: " + reason);

        if(lastKnownWindow != player.getCurrentWindowIndex()) {
            Track previous = lastKnownWindow == C.INDEX_UNSET ? null : queue.get(lastKnownWindow);
            Track next = getCurrentTrack();

            // Track changed because it ended
            // We'll use its duration instead of the last known position
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION && lastKnownWindow != C.INDEX_UNSET) {
                if (lastKnownWindow >= player.getCurrentTimeline().getWindowCount()) return;
                long duration = player.getCurrentTimeline().getWindow(lastKnownWindow, new Window()).getDurationMs();
                if(duration != C.TIME_UNSET) lastKnownPosition = duration;
            }

            manager.onTrackUpdate(previous, lastKnownPosition, next);
        }

        lastKnownWindow = player.getCurrentWindowIndex();
        lastKnownPosition = player.getCurrentPosition();
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Buffering updates
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        int state = getState();

        if(state != previousState) {
            if(Utils.isPlaying(state) && !Utils.isPlaying(previousState)) {
                manager.onPlay();
            } else if(Utils.isPaused(state) && !Utils.isPaused(previousState)) {
                manager.onPause();
            } else if(Utils.isStopped(state) && !Utils.isStopped(previousState)) {
                manager.onStop();
            }

            manager.onStateChange(state);
            previousState = state;

            if(state == PlaybackStateCompat.STATE_STOPPED) {
                manager.onEnd(getCurrentTrack(), getPosition());
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        // Repeat mode update
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        // Shuffle mode update
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        String code;

        if(error.type == ExoPlaybackException.TYPE_SOURCE) {
            code = "playback-source";
        } else if(error.type == ExoPlaybackException.TYPE_RENDERER) {
            code = "playback-renderer";
        } else {
            code = "playback"; // Other unexpected errors related to the playback
        }

        manager.onError(code, error.getCause().getMessage());
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        // Speed or pitch changes
    }

    @Override
    public void onSeekProcessed() {
        // Finished seeking
    }

    @Override
    public void onMetadata(Metadata metadata) {
        String source = null;
        String title = null, url = null, artist = null, album = null, date = null, genre = null;

        for(int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            // The ID3 Tag List can be found in
            // https://en.wikipedia.org/wiki/ID3

            if(entry instanceof TextInformationFrame) {
                // ID3 text tag
                TextInformationFrame id3 = (TextInformationFrame)entry;
                source = "id3";

                if (id3.id.equals("TIT2")) {
                    title = id3.value;
                } else if (id3.id.equals("TALB") || id3.id.equals("TOAL")) {
                    album = id3.value;
                } else if (id3.id.equals("TOPE")) {
                    artist = id3.value;
                } else if (id3.id.equals("TDRC")) {
                    date = id3.value;
                }

            } else if (entry instanceof UrlLinkFrame) {
                UrlLinkFrame id3 = (UrlLinkFrame)entry;
                source = "id3";

                if (id3.id.equals("WOAS") || id3.id.equals("WORS") || id3.id.equals("WOAF")) {
                    url = id3.url;
                }

            } else if(entry instanceof IcyHeaders) {
                // ICY headers - Only fills missing information
                IcyHeaders icy = (IcyHeaders)entry;

                if(source == null) source = "icy-headers";
                if(title == null) title = icy.name;
                if(url == null) url = icy.url;
                if(genre == null) genre = icy.genre;

            } else if(entry instanceof IcyInfo) {
                // ICY data
                IcyInfo icy = (IcyInfo)entry;
                source = "icy";

                int index = icy.title == null ? -1 : icy.title.indexOf(" - ");

                if (index != -1) {
                    artist = icy.title.substring(0, index);
                    title = icy.title.substring(index + 3);
                } else {
                    title = icy.title;
                }

                url = icy.url;

            }

        }

        if (source != null) {
            manager.onMetadataReceived(source, title, url, artist, album, date, genre);
        }
    }
}
