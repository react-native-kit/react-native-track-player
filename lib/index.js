import { Platform, AppRegistry, DeviceEventEmitter, NativeEventEmitter, NativeModules } from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

const { TrackPlayerModule: TrackPlayer } = NativeModules;
const emitter = Platform.OS !== 'android' ? new NativeEventEmitter(TrackPlayer) : DeviceEventEmitter;

function resolveAsset(uri) {
    if(!uri) return undefined;
    return resolveAssetSource(uri);
}

function resolveUrl(url) {
    if(!url) return undefined;
    return resolveAssetSource(url) || url;
}

function setupPlayer(options) {
    return TrackPlayer.setupPlayer(options || {});
}

function updateOptions(data) {
    // Clone the object before modifying it, so we don't run into problems with immutable objects
    data = Object.assign({}, data);

    // Resolve the asset for each icon
    data.icon = resolveAsset(data.icon);
    data.playIcon = resolveAsset(data.playIcon);
    data.pauseIcon = resolveAsset(data.pauseIcon);
    data.stopIcon = resolveAsset(data.stopIcon);
    data.previousIcon = resolveAsset(data.previousIcon);
    data.nextIcon = resolveAsset(data.nextIcon);
    data.rewindIcon = resolveAsset(data.rewindIcon);
    data.forwardIcon = resolveAsset(data.forwardIcon);

    return TrackPlayer.updateOptions(data);
}

function add(tracks, insertBeforeId) {
    // Clone the array before modifying it, so we don't run into problems with immutable objects
    if(Array.isArray(tracks)) {
      tracks = [...tracks];
    } else {
      tracks = [tracks];
    }

    if(tracks.length < 1) return;

    for(let i = 0; i < tracks.length; i++) {
        // Clone the object before modifying it
        tracks[i] = Object.assign({}, tracks[i]);

        // Resolve the URLs
        tracks[i].url = resolveUrl(tracks[i].url);
        tracks[i].artwork = resolveUrl(tracks[i].artwork);

        // Cast ID's into strings
        tracks[i].id = `${tracks[i].id}`
    }

    return TrackPlayer.add(tracks, insertBeforeId);
}

function remove(tracks) {
    if(!Array.isArray(tracks)) {
        tracks = [tracks];
    }

    return TrackPlayer.remove(tracks);
}

function updateMetadataForTrack(id, metadata) {
    // Clone the object before modifying it
    metadata = Object.assign({}, metadata);

    // Resolve the artowork URL
    metadata.artwork = resolveUrl(metadata.artwork);

    return TrackPlayer.updateMetadataForTrack(id, metadata);
}

function registerPlaybackService(serviceFactory) {
    if (Platform.OS === 'android') {
        // Registers the headless task
        AppRegistry.registerHeadlessTask('TrackPlayer', serviceFactory);
    } else {
        // Initializes and runs the service in the next tick
        setImmediate(serviceFactory());
    }
}

function addEventListener(event, listener) {
    return emitter.addListener(event, listener);
}

function warpEventResponse(handler, event, payload) {
    // transform into the old format and return to handler
    const additionalKeys = payload || {};
    handler({ type: event, ...additionalKeys });
}

/**
 * @deprecated since version 1.0.1. Use addEventListener instead.
 */
function registerEventHandler(handler) {
    let events = [
        'playback-state',
        'playback-error',
        'playback-queue-ended',
        'playback-track-changed',

        'remote-play',
        'remote-pause',
        'remote-stop',
        'remote-next',
        'remote-previous',
        'remote-jump-forward',
        'remote-jump-backward',
        'remote-seek',
        'remote-duck',
    ];
  
    if (Platform.OS === 'android') {
        events.push('remote-skip', 'remote-set-rating', 'remote-play-id', 'remote-play-search');
    }

    registerPlaybackService(() => {
        return async function() {
            for (let i = 0; i < events.length; i++) {
                addEventListener(events[i], warpEventResponse.bind(null, handler, events[i]));
            }
        };
    });
}

// We'll declare each one of the constants and functions manually so IDEs can show a list of them
// We should also add documentation here, but I'll leave this task to another day

// States
module.exports.STATE_NONE = TrackPlayer.STATE_NONE;
module.exports.STATE_READY = TrackPlayer.STATE_READY;
module.exports.STATE_PLAYING = TrackPlayer.STATE_PLAYING;
module.exports.STATE_PAUSED = TrackPlayer.STATE_PAUSED;
module.exports.STATE_STOPPED = TrackPlayer.STATE_STOPPED;
module.exports.STATE_BUFFERING = TrackPlayer.STATE_BUFFERING;
module.exports.STATE_CONNECTING = TrackPlayer.STATE_CONNECTING;

// Capabilities
module.exports.CAPABILITY_PLAY = TrackPlayer.CAPABILITY_PLAY;
module.exports.CAPABILITY_PLAY_FROM_ID = TrackPlayer.CAPABILITY_PLAY_FROM_ID;
module.exports.CAPABILITY_PLAY_FROM_SEARCH = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH;
module.exports.CAPABILITY_PAUSE = TrackPlayer.CAPABILITY_PAUSE;
module.exports.CAPABILITY_STOP = TrackPlayer.CAPABILITY_STOP;
module.exports.CAPABILITY_SEEK_TO = TrackPlayer.CAPABILITY_SEEK_TO;
module.exports.CAPABILITY_SKIP = TrackPlayer.CAPABILITY_SKIP;
module.exports.CAPABILITY_SKIP_TO_NEXT = TrackPlayer.CAPABILITY_SKIP_TO_NEXT;
module.exports.CAPABILITY_SKIP_TO_PREVIOUS = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS;
module.exports.CAPABILITY_JUMP_FORWARD = TrackPlayer.CAPABILITY_JUMP_FORWARD;
module.exports.CAPABILITY_JUMP_BACKWARD = TrackPlayer.CAPABILITY_JUMP_BACKWARD;
module.exports.CAPABILITY_SET_RATING = TrackPlayer.CAPABILITY_SET_RATING;
module.exports.CAPABILITY_LIKE = TrackPlayer.CAPABILITY_LIKE;
module.exports.CAPABILITY_DISLIKE = TrackPlayer.CAPABILITY_DISLIKE;
module.exports.CAPABILITY_BOOKMARK = TrackPlayer.CAPABILITY_BOOKMARK;

// Pitch algorithms
module.exports.PITCH_ALGORITHM_LINEAR = TrackPlayer.PITCH_ALGORITHM_LINEAR;
module.exports.PITCH_ALGORITHM_MUSIC = TrackPlayer.PITCH_ALGORITHM_MUSIC;
module.exports.PITCH_ALGORITHM_VOICE = TrackPlayer.PITCH_ALGORITHM_VOICE;

// Rating Types
module.exports.RATING_HEART = TrackPlayer.RATING_HEART;
module.exports.RATING_THUMBS_UP_DOWN = TrackPlayer.RATING_THUMBS_UP_DOWN;
module.exports.RATING_3_STARS = TrackPlayer.RATING_3_STARS;
module.exports.RATING_4_STARS = TrackPlayer.RATING_4_STARS;
module.exports.RATING_5_STARS = TrackPlayer.RATING_5_STARS;
module.exports.RATING_PERCENTAGE = TrackPlayer.RATING_PERCENTAGE;

// General
module.exports.setupPlayer = setupPlayer;
module.exports.destroy = TrackPlayer.destroy;
module.exports.updateOptions = updateOptions;
module.exports.isServiceRunning = TrackPlayer.isServiceRunning;
module.exports.registerEventHandler = registerEventHandler;
module.exports.registerBackendService = registerPlaybackService;
module.exports.registerPlaybackService = registerPlaybackService;
module.exports.addEventListener = addEventListener;

// Player Queue Commands
module.exports.add = add;
module.exports.remove = remove;
module.exports.skip = TrackPlayer.skip;
module.exports.getQueue = TrackPlayer.getQueue;
module.exports.skipToNext = TrackPlayer.skipToNext;
module.exports.skipToPrevious = TrackPlayer.skipToPrevious;
module.exports.updateMetadataForTrack = updateMetadataForTrack;
module.exports.removeUpcomingTracks = TrackPlayer.removeUpcomingTracks;

// Player Playback Commands
module.exports.reset = TrackPlayer.reset;
module.exports.play = TrackPlayer.play;
module.exports.pause = TrackPlayer.pause;
module.exports.stop = TrackPlayer.stop;
module.exports.seekTo = TrackPlayer.seekTo;
module.exports.setVolume = TrackPlayer.setVolume;
module.exports.setRate = TrackPlayer.setRate;

// Player Getters
module.exports.getTrack = TrackPlayer.getTrack;
module.exports.getCurrentTrack = TrackPlayer.getCurrentTrack;
module.exports.getVolume = TrackPlayer.getVolume;
module.exports.getDuration = TrackPlayer.getDuration;
module.exports.getPosition = TrackPlayer.getPosition;
module.exports.getBufferedPosition = TrackPlayer.getBufferedPosition;
module.exports.getState = TrackPlayer.getState;
module.exports.getRate = TrackPlayer.getRate;

// Player Event Types
module.exports.TrackPlayerEvents = require('./eventTypes');

// Components
module.exports.ProgressComponent = require('./ProgressComponent');

// React Hooks (Requires React v16.8+ and React Native v0.59+)
const hooks = require('./hooks');
module.exports = { ...module.exports, ...hooks }
