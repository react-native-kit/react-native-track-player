import React, { useEffect } from "react";
import { StyleSheet, Text, View } from "react-native";
import TrackPlayer, { usePlaybackState, useTrackPlayerProgress } from "react-native-track-player";

import Player from "../components/Player";
import playlistData from "../data/playlist.json";
import localTrack from "../resources/pure.m4a";

export default function LandingScreen() {
  const playbackState = usePlaybackState();
  const trackPlayerProcess = useTrackPlayerProgress();

  useEffect(() => {
    TrackPlayer.setupPlayer();
    TrackPlayer.updateOptions({
      stopWithApp: true,
      capabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE,
        TrackPlayer.CAPABILITY_SKIP_TO_NEXT,
        TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS,
        TrackPlayer.CAPABILITY_STOP
      ],
      compactCapabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE
      ]
    });
  }, []);

  async function togglePlayback() {
    const currentTrack = await TrackPlayer.getCurrentTrack();
    if (currentTrack == null) {
      await TrackPlayer.reset();
      await TrackPlayer.add(playlistData);
      await TrackPlayer.add({
        id: "local-track",
        url: localTrack,
        title: "Pure (Demo)",
        artist: "David Chavez",
        artwork: "https://picsum.photos/200"
      });
      await TrackPlayer.play();
    } else {
      if (playbackState === TrackPlayer.STATE_PAUSED) {
        await TrackPlayer.play();
      } else {
        await TrackPlayer.pause();
      }
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.description}>
        We'll be inserting a playlist into the library loaded from
        `playlist.json`. We'll also be using the `ProgressComponent` which
        allows us to track playback time.
      </Text>
      <Player
        onNext={skipToNext}
        style={styles.player}
        onPrevious={skipToPrevious}
        onTogglePlayback={togglePlayback}
      />
      <View style={styles.state}>
        <Text>{getStateName(playbackState)}</Text>
        <Text>Current: {trackPlayerProcess.position | 0} sec</Text>
        <Text>Buffered: {trackPlayerProcess.bufferedPosition | 0} sec</Text>
        <Text>Duration: {trackPlayerProcess.duration | 0} sec</Text>
      </View>
    </View>
  );
}

LandingScreen.navigationOptions = {
  title: "Playlist Example"
};

function getStateName(state) {
  switch (state) {
    case TrackPlayer.STATE_NONE:
      return "None";
    case TrackPlayer.STATE_PLAYING:
      return "Playing";
    case TrackPlayer.STATE_PAUSED:
      return "Paused";
    case TrackPlayer.STATE_STOPPED:
      return "Stopped";
    case TrackPlayer.STATE_BUFFERING:
      return "Buffering";
  }
}

async function skipToNext() {
  try {
    await TrackPlayer.skipToNext();
  } catch (_) {}
}

async function skipToPrevious() {
  try {
    await TrackPlayer.skipToPrevious();
  } catch (_) {}
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  description: {
    width: "80%",
    marginTop: 20,
    textAlign: "center"
  },
  player: {
    marginTop: 40
  },
  state: {
    marginTop: 20
  }
});
