---
title: Documentation
permalink: /documentation/
---

## Summary

* [Constants](#constants)
  * [State](#state)
  * [Rating](#rating)
  * [Capability](#capability)
  * [Pitch Algorithm](#pitch-algorithm)
* [Functions](#functions)
  * [Lifecycle](#lifecycle-functions)
  * [Queue](#queue-functions)
  * [Player](#player-functions)
* [Events](#events)
  * [Media Controls](#media-controls)
  * [Player](#player)
* [Components](#components)
  * [ProgressComponent](#progresscomponent)
* [Objects](#objects)
  * [Track Object](#track-object)
  * [Resource Object](#resource-object)

## Constants
### State
#### `STATE_NONE`
State indicating that no media is currently loaded
#### `STATE_READY`
State indicating that the player is ready to start playing
#### `STATE_PLAYING`
State indicating that the player is currently playing
#### `STATE_PAUSED`
State indicating that the player is currently paused
#### `STATE_STOPPED`
State indicating that the player is currently stopped
#### `STATE_BUFFERING`
State indicating that the player is currently buffering (in "play" state)
#### `STATE_CONNECTING`
State indicating that the player is currently buffering (in "pause" state)

### Rating
#### `RATING_HEART`
Rating type indicating "with heart" or "without heart", its value is a `boolean`.
#### `RATING_THUMBS_UP_DOWN`
Rating type indicating "thumbs up" or "thumbs down", its value is a `boolean`.
#### `RATING_3_STARS`
Rating type indicating 0 to 3 stars, its value is a `number` of stars.
#### `RATING_4_STARS`
Rating type indicating 0 to 4 stars, its value is a `number` of stars.
#### `RATING_5_STARS`
Rating type indicating 0 to 5 stars, its value is a `number` of stars.
#### `RATING_PERCENTAGE`
Rating type indicating percentage, its value is a `number`.

### Capability
#### `CAPABILITY_PLAY`
Capability indicating the ability to play
#### `CAPABILITY_PLAY_FROM_ID`
Capability indicating the ability to play from a track id (Required for Android Auto)
#### `CAPABILITY_PLAY_FROM_SEARCH`
Capability indicating the ability to play from a text/voice search (Required for Android Auto)
#### `CAPABILITY_PAUSE`
Capability indicating the ability to pause
#### `CAPABILITY_STOP`
Capability indicating the ability to stop
#### `CAPABILITY_SEEK_TO`
Capability indicating the ability to seek to a position in the timeline
#### `CAPABILITY_SKIP`
Capability indicating the ability to skip to any song in the queue
#### `CAPABILITY_SKIP_TO_NEXT`
Capability indicating the ability to skip to the next track
#### `CAPABILITY_SKIP_TO_PREVIOUS`
Capability indicating the ability to skip to the previous track
#### `CAPABILITY_SET_RATING`
Capability indicating the ability to set the rating value based on the rating type
#### `CAPABILITY_JUMP_FORWARD`
Capability indicating the ability to jump forward by the amount of seconds specified in the options
#### `CAPABILITY_JUMP_BACKWARD`
Capability indicating the ability to jump backward by the amount of seconds specified in the options

### Pitch Algorithm
#### `PITCH_ALGORITHM_LINEAR`
An algorithm suitable for general use.
#### `PITCH_ALGORITHM_MUSIC`
An algorithm suitable for music.
#### `PITCH_ALGORITHM_VOICE`
An algorithm suitable for voice.

## Functions
### Lifecycle Functions
#### `setupPlayer(options)`
Initializes the player with the specified options. These options do not apply to all platforms, see chart below.

These options are different than the ones set using `updateOptions()`. Options other than those listed below will not be applied.

You should always call this function (even without any options set) before using the player to make sure everything is initialized.

If the player is already initialized, the promise will resolve instantly.

**Returns:** `Promise`

| Param                | Type     | Description   | Default   | Android | iOS | Windows |
| -------------------- | -------- | ------------- | --------- | :-----: | :-: | :-----: |
| options              | `object` | The options   |
| options.minBuffer    | `number` | Minimum time in seconds that needs to be buffered | 15 | ✓ | ✗ | ✗ |
| options.maxBuffer    | `number` | Maximum time in seconds that needs to be buffered | 50 | ✓ | ✗ | ✗ |
| options.playBuffer   | `number` | Minimum time in seconds that needs to be buffered to start playing | 2.5 | ✓ | ✗ | ✗ |
| options.backBuffer   | `number` | Time in seconds that should be kept in the buffer behind the current playhead time. | 0 | ✓ | ✗ | ✗ |
| options.maxCacheSize | `number` | Maximum cache size in kilobytes | 0 | ✓ | ✗ | ✗ |
| options.iosCategory  | `string` | [AVAudioSession.Category](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616615-category) for iOS. Sets on `play()` | `playback` | ✗ | ✓ | ✗ |
| options.iosCategoryOptions | `array` | [AVAudioSession.CategoryOptions](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616503-categoryoptions) for iOS. Sets on `play()` | `[]` | ✗ | ✓ | ✗ |
| options.iosCategoryMode  | `string` | [AVAudioSession.Mode](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616508-mode) for iOS. Sets on `play()` | `default` | ✗ | ✓ | ✗ |

#### `destroy()`
Destroys the player, cleaning up its resources. After executing this function, you won't be able to use the player anymore, unless you call `setupPlayer()` again.

#### `registerPlaybackService(serviceProvider)`
Register the playback service. The service will run as long as the player runs.

This function should only be called once, and should be registered right after registering your React application with `AppRegistry`.

You should use the playback service to register the event handlers that must be directly tied to the player, as the playback service might keep running when the app is in background.

| Param   | Type     | Description   |
| ------- | -------- | ------------- |
| serviceProvider | `function` | The function that must return an async service function. |

#### `addEventListener(event, listener)`
Adds a new event listener.

You can find a list of events in the [events section](#events).

This function returns an event subscription instance that can be stored and removed after by executing `remove()`.

**Returns:** `EmitterSubscription`

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| event    | `string` | The event name |
| listener | `function(data)` | The listener function |

#### `registerEventHandler(handler)`
**DEPRECATED**: Use registerPlaybackService and addEventListener instead.

Registers an event handler. This function should only be called once, and should be registered right after registering your React application with `AppRegistry`.

| Param   | Type     | Description   |
| ------- | -------- | ------------- |
| handler | `function` | The function that acts as an event handler |

### Queue Functions
#### `add(tracks, insertBeforeId)`
Adds one or more tracks to the queue.

**Returns:** `Promise`

| Param          | Type     | Description   |
| -------------- | -------- | ------------- |
| tracks         | `array` of [Track Object](#track-object) or a single one | The tracks that will be added |
| insertBeforeId | `string` | The ID of the track that will be located immediately after the inserted tracks. Set it to `null` to add it at the end of the queue |

#### `remove(tracks)`
Removes one or more tracks from the queue.

**Returns:** `Promise`

| Param  | Type     | Description   |
| ------ | -------- | ------------- |
| tracks | `array` of track ids or a single one | The tracks that will be removed |

#### `skip(id)`
Skips to a track in the queue.

**Returns:** `Promise`

| Param  | Type     | Description   |
| ------ | -------- | ------------- |
| id  | `string` | The track id  |

#### `skipToNext()`
Skips to the next track in the queue.

**Returns:** `Promise`

#### `skipToPrevious()`
Skips to the previous track in the queue.

**Returns:** `Promise`

#### `reset()`
Resets the player stopping the current track and clearing the queue.

#### `getTrack(id)`
Gets a track object from the queue.

**Returns:** `Promise<`Object as described in [Track Object](#track-object)`>`

| Param    | Type       | Description   |
| -------- | ---------- | ------------- |
| id       | `string`   | The track ID  |

#### `getCurrentTrack()`
Gets the id of the current track

**Returns:** `Promise<string>`

#### `getQueue()`
Gets the whole queue

**Returns:** `Promise<Array<`Object as described in [Track Object](#track-object)`>>`

#### `removeUpcomingTracks()`
Clears any upcoming tracks from the queue.

#### `updateMetadataForTrack(id, metadata)`
Updates the metadata of a track in the queue.
If the current track is updated, the notification and the Now Playing Center will be updated accordingly.

**Returns:** `Promise`

| Param    | Type       | Description   |
| -------- | ---------- | ------------- |
| id       | `string`   | The track ID  |
| metadata | `object`   | A subset of the [Track Object](#track-object) with only the `artwork`, `title`, `artist`, `album`, `description`, `genre`, `date`, `rating` and `duration` properties. |

### Player Functions
#### `updateOptions(options)`
Updates the configuration for the components.

All parameters are optional. You also only need to specify the ones you want to update.

These parameters are different than the ones set using `setupPlayer()`. Parameters other than those listed below will not be applied.

Some parameters are unused depending on platform.

| Param     | Type       | Description          | Android | iOS | Windows |
| --------- | ---------- | -------------------- | :-----: | :-: | :-----: |
| options      | `object`   | The options |
| options.ratingType | [Rating Constant](#rating) | The rating type | ✓ | ✗ | ✗ |
| options.jumpInterval | `number` | The interval in seconds for the jump forward/backward buttons | ✓ | ✓ | ✓ |
| options.stopWithApp | `boolean` | Whether the player will be destroyed when the app closes | ✓ | ✗ | ✗ |
| options.alwaysPauseOnInterruption | `boolean` | Whether the `remote-duck` event will be triggered on every interruption | ✓ | ✗ | ✗ |
| options.isLiveStreamSource | `boolean` | If iOS lock screen player should display progress or 'Live' | ✗ | ✓ | ✗ |
| options.capabilities | `array` of [Capability Constants](#capability) | The media controls that will be enabled | ✓ | ✓ | ✓ |
| options.notificationCapabilities | `array` of [Capability Constants](#capability) | The buttons that it will show in the notification. Defaults to `data.capabilities`  | ✓ | ✗ | ✗ |
| options.compactCapabilities | `array` of [Capability Constants](#capability) | The buttons that it will show in the compact notification | ✓ | ✗ | ✗ |
| options.icon | [Resource Object](#resource-object) | The notification icon | ✓ | ✗ | ✗ |
| options.playIcon | [Resource Object](#resource-object) | The play icon | ✓ | ✗ | ✗ |
| options.pauseIcon | [Resource Object](#resource-object) | The pause icon | ✓ | ✗ | ✗ |
| options.stopIcon | [Resource Object](#resource-object) | The stop icon | ✓ | ✗ | ✗ |
| options.previousIcon | [Resource Object](#resource-object) | The previous icon | ✓ | ✗ | ✗ |
| options.nextIcon | [Resource Object](#resource-object) | The next icon | ✓ | ✗ | ✗ |
| options.rewindIcon | [Resource Object](#resource-object) | The jump backward icon | ✓ | ✗ | ✗ |
| options.forwardIcon | [Resource Object](#resource-object) | The jump forward icon | ✓ | ✗ | ✗ |
| options.color | `number` | The notification color in an ARGB hex | ✓ | ✗ | ✗ |

#### `play()`
Plays or resumes the current track.

#### `pause()`
Pauses the current track.

#### `stop()`
Stops the current track.

#### `seekTo(seconds)`
Seeks to a specified time position in the current track.

| Param   | Type     | Description             |
| ------- | -------- | ----------------------- |
| seconds | `number` | The position in seconds |

#### `setVolume(volume)`
Sets the volume of the player.

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| volume | `number` | The volume in a range from 0 to 1 |

#### `getVolume()`
Gets the volume of the player (a number between 0 and 1).

**Returns:** `Promise<number>`

#### `setRate(rate)`
Sets the playback rate

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| rate   | `number` | The playback rate where 1 is the regular speed |

#### `getRate()`
Gets the playback rate, where 1 is the regular speed.

**Returns:** `Promise<number>`

#### `getDuration()`
Gets the duration of the current track in seconds.

Note: `react-native-track-player` is a streaming library, which means it slowly buffers the track and doesn't know exactly when it ends.
The duration returned by this function is determined through various tricks and *may not be exact or may not be available at all*.

You should **not** trust this function. You should retrieve the duration from a database and feed it to the `duration` parameter in the [Track Object](#track-object).

**Returns:** `Promise<number>`

#### `getPosition()`
Gets the position of the player in seconds.

**Returns:** `Promise<number>`

#### `getBufferedPosition()`
Gets the buffered position of the player in seconds.

**Returns:** `Promise<number>`

#### `getState()`
Gets the state of the player.

**Returns:** `Promise<`[State Constant](#state)`>`

## Events

All event types are made available through the named export `TrackPlayerEvents`:

```js
import { TrackPlayerEvents } from 'react-native-track-player';
```

### Media Controls

#### `remote-play`
Fired when the user presses the play button. Only fired if the `CAPABILITY_PLAY` is allowed.

#### `remote-play-id`
Fired when the user selects a track from an external device. Required for Android Auto support. Only fired if the `CAPABILITY_PLAY_FROM_ID` is allowed.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| id    | `string` | The track id  |

#### `remote-play-search`
Fired when the user searches for a track (usually voice search). Required for Android Auto support. Only fired if the `CAPABILITY_PLAY_FROM_SEARCH` is allowed.

Every parameter except `query` is optional and may not be provided.
In the case where `query` is empty, feel free to select any track to play.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| query    | `string` | The search query |
| focus    | `string` | The focus of the search. One of `artist`, `album`, `playlist` or `genre` |
| title    | `string` | The track title |
| artist   | `string` | The track artist |
| album    | `string` | The track album |
| genre    | `string` | The track genre |
| playlist | `string` | The track playlist |

#### `remote-pause`
Fired when the user presses the pause button. Only fired if the `CAPABILITY_PAUSE` is allowed or if there's a change in outputs (e.g.: headphone disconnected).

#### `remote-stop`
Fired when the user presses the stop button. Only fired if the `CAPABILITY_STOP` is allowed.

#### `remote-skip`
Fired when the user skips to a track in the queue. Only fired if the `CAPABILITY_SKIP` is allowed.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| id    | `string` | The track id  |

#### `remote-next`
Fired when the user presses the next track button. Only fired if the `CAPABILITY_SKIP_TO_NEXT` is allowed.

#### `remote-previous`
Fired when the user presses the previous track button. Only fired if the `CAPABILITY_SKIP_TO_PREVIOUS` is allowed.

#### `remote-seek`
Fired when the user changes the position of the timeline. Only fired if the `CAPABILITY_SEEK_TO` is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| position | `number` | The position in seconds |

#### `remote-set-rating`
Fired when the user changes the rating for the track. Only fired if the `CAPABILITY_SET_RATING` is allowed.

| Param  | Type     | Description   |
| ------ | -------- | ------------- |
| rating | Depends on the [Rating Type](#rating) | The rating that was set |

#### `remote-jump-forward`
Fired when the user presses the jump forward button. Only fired if the `CAPABILITY_JUMP_FORWARD` is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| interval | `number` | The number of seconds to jump forward. It's usually the `jumpInterval` set in the options. |

#### `remote-jump-backward`
Fired when the user presses the jump backward button. Only fired if the `CAPABILITY_JUMP_BACKWARD` is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| interval | `number` | The number of seconds to jump backward. It's usually the `jumpInterval` set in the options. |

#### `remote-duck`
Fired when the device needs the player to pause for a interruption.

The volume may also be lowered on an transient interruption without triggering this event.
If you want to receive those interruptions, set the `alwaysPauseOnInterruption` option to true.

- When the event is triggered with `permanent` set to true, you should stop the playback.
- When the event is triggered with `paused` set to true, you should pause the playback. It will also be set to true when `permanent` is true.
- When the event is triggered and none of them are set to true, you should resume the track.

| Param     | Type      | Description                                  |
| --------- | --------- | -------------------------------------------- |
| paused    | `boolean` | Whether the player should pause the playback |
| permanent | `boolean` | Whether the player should stop the playback  |

### Player
#### `playback-state`
Fired when the state of the player changes.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| state | [State Constant](#state) | The new state |

#### `playback-track-changed`
Fired when a track is changed.

| Param     | Type     | Description                            |
| --------- | -------- | -------------------------------------- |
| track     | `string` | The previous track id. Might be null   |
| position  | `number` | The previous track position in seconds |
| nextTrack | `string` | The next track id. Might be null       |

#### `playback-queue-ended`
Fired when the queue reaches the end.

| Param    | Type     | Description                               |
| -------- | -------- | ----------------------------------------- |
| track    | `string` | The previous track id. Might be null      |
| position | `number` | The previous track position in seconds    |

#### `playback-metadata-received`
Fired when the current track receives metadata encoded in. (e.g. ID3 tags or Icy Metadata).

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| source   | `string` | The metadata source (`id3`, `icy` or `icy-headers`) |
| title    | `string` | The track title. Might be null                      |
| url      | `string` | The track url. Might be null                        |
| artist   | `string` | The track artist. Might be null                     |
| album    | `string` | The track album. Might be null                      |
| date     | `string` | The track date. Might be null                       |
| genre    | `string` | The track genre. Might be null                      |

#### `playback-error`
Fired when an error occurs.

| Param   | Type     | Description       |
| ------- | -------- | ----------------- |
| code    | `string` | The error code    |
| message | `string` | The error message |

## Components
#### `ProgressComponent`
A component base that updates itself every second with a new position. Your app should extend it with a custom render.

| State            | Type     | Description                      |
| ---------------- | -------- | -------------------------------- |
| position         | `number` | The current position in seconds  |
| bufferedPosition | `number` | The buffered position in seconds |
| duration         | `number` | The duration in seconds          |

| Functions           | Return Type | Description                                     |
| ------------------- | ----------- | ----------------------------------------------- |
| getProgress         | `number`    | The current progress expressed between 0 and 1  |
| getBufferedProgress | `number`    | The buffered progress expressed between 0 and 1 |

## Objects
### Track Object
Tracks in the player queue are plain javascript objects as described below.

Only the `id`, `url`, `title` and `artist` properties are required for basic playback

| Param          | Type                        | Description  |
| -------------- | --------------------------- | ------------ |
| id             | `string`                    | The track id                |
| url            | `string` or [Resource Object](#resource-object) | The media URL |
| type           | `string`                    | Stream type. One of `dash`, `hls`, `smoothstreaming` or `default` |
| userAgent      | `string`                    | The user agent HTTP header  |
| contentType    | `string`                    | Mime type of the media file |
| duration       | `number`                    | The duration in seconds     |
| title          | `string`                    | The track title             |
| artist         | `string`                    | The track artist            |
| album          | `string`                    | The track album             |
| description    | `string`                    | The track description       |
| genre          | `string`                    | The track genre             |
| date           | `string`                    | The track release date in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) |
| rating         | Depends on the [rating type](#rating)  | The track rating value |
| artwork        | `string` or [Resource Object](#resource-object) | The artwork url |
| pitchAlgorithm | [Pitch Algorithm](#pitch-algorithm) | The pitch algorithm |
| headers        | `object`                    | An object containing all the headers to use in the HTTP request |

### Resource Object
Resource objects are the result of `require`/`import` for files.

For more information about Resource Objects, read the [Images](https://facebook.github.io/react-native/docs/images.html) section of the React Native documentation

## React Hooks

React v16.8 introduced [hooks](https://reactjs.org/docs/hooks-intro.html). If you are using a version of React Native that is before [v0.59.0](https://facebook.github.io/react-native/blog/2019/03/12/releasing-react-native-059), your React Native version does not support hooks.

#### useTrackPlayerEvents
Register an event listener for one or more of the [events](#events) emitted by the TrackPlayer. The subscription is removed when the component unmounts.

Check out the [events section](#events) for a full list of supported events.

```jsx
import React, { useState } from 'react';
import { Text, View } from 'react-native';
import { useTrackPlayerEvent, TrackPlayerEvents, STATE_PLAYING } from 'react-native-track-player';

// Subscribing to the following events inside MyComponent
const events = [
  TrackPlayerEvents.PLAYBACK_STATE,
  TrackPlayerEvents.PLAYBACK_ERROR
];

const MyComponent = () => {
  const [playerState, setState] = useState(null)

  useTrackPlayerEvents(events, (event) => {
    if (event.type === TrackPlayerEvents.PLAYBACK_ERROR) {
      console.warn('An error occurred while playing the current track.');
    }
    if (event.type === TrackPlayerEvents.PLAYBACK_STATE) {
      setState(playbackState)
    }
  });

  const isPlaying = playerState === STATE_PLAYING;

  return (
    <View>
      <Text>The TrackPlayer is {isPlaying ? 'playing' : 'not playing'}</Text>
    </View>
  );
};
```

#### useTrackPlayerProgress
A hook alternative to the [Progress Component](#progresscomponent).

| State            | Type     | Description                      |
| ---------------- | -------- | -------------------------------- |
| position         | `number` | The current position in seconds  |
| bufferedPosition | `number` | The buffered position in seconds |
| duration         | `number` | The duration in seconds          |

`useTrackPlayerProgress` accepts an interval to set the rate (in miliseconds) to poll the track player's progress. The default value is `1000` or every second.

```jsx
import React from 'react';
import { Text, View } from 'react-native';
import { useTrackPlayerProgress } from 'react-native-track-player';

const MyComponent = () => {
  const { position, bufferedPosition, duration } = useTrackPlayerProgress()

  return (
    <View>
      <Text>Track progress: {position} seconds out of {duration} total</Text>
      <Text>Buffered progress: {bufferedPosition} seconds buffered out of {duration} total</Text>
    </View>
  )
}
```
