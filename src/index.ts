import { Platform, AppRegistry, DeviceEventEmitter, NativeEventEmitter, NativeModules } from 'react-native'
// @ts-ignore
import * as resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource'
import { MetadataOptions, PlayerOptions, Event, Track, State, TrackMetadata } from './interfaces'

const { TrackPlayerModule: TrackPlayer } = NativeModules
const emitter = Platform.OS !== 'android' ? new NativeEventEmitter(TrackPlayer) : DeviceEventEmitter

// MARK: - Helpers

function resolveImportedPath(path?: number | string) {
  if (!path) return undefined
  return resolveAssetSource(path) || path
}

// MARK: - General API

async function setupPlayer(options: PlayerOptions = {}): Promise<void> {
  return TrackPlayer.setupPlayer(options || {})
}

function destroy() {
  return TrackPlayer.destroy()
}

async function updateOptions(options: MetadataOptions = {}): Promise<void> {
  options = { ...options }

  // Resolve the asset for each icon
  options.icon = resolveImportedPath(options.icon)
  options.playIcon = resolveImportedPath(options.playIcon)
  options.pauseIcon = resolveImportedPath(options.pauseIcon)
  options.stopIcon = resolveImportedPath(options.stopIcon)
  options.previousIcon = resolveImportedPath(options.previousIcon)
  options.nextIcon = resolveImportedPath(options.nextIcon)
  options.rewindIcon = resolveImportedPath(options.rewindIcon)
  options.forwardIcon = resolveImportedPath(options.forwardIcon)

  return TrackPlayer.updateOptions(options)
}

type ServiceHandler = () => Promise<void>
function registerPlaybackService(factory: () => ServiceHandler) {
  if (Platform.OS === 'android') {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory)
  } else {
    // Initializes and runs the service in the next tick
    setImmediate(factory())
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addEventListener(event: Event, listener: (data: any) => void) {
  return emitter.addListener(event, listener)
}

// MARK: - Queue API

async function add(tracks: Track | Track[], insertBeforeId?: string): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks]
  }

  if (tracks.length < 1) return

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = { ...tracks[i] }

    // Resolve the URLs
    tracks[i].url = resolveImportedPath(tracks[i].url)
    tracks[i].artwork = resolveImportedPath(tracks[i].artwork)

    // Cast ID's into strings
    tracks[i].id = `${tracks[i].id}`
  }

  return TrackPlayer.add(tracks, insertBeforeId)
}

async function remove(tracks: Track | Track[]): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks]
  }

  return TrackPlayer.remove(tracks)
}

async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks()
}

async function skip(trackId: string): Promise<void> {
  return TrackPlayer.skip(trackId)
}

async function skipToNext(): Promise<void> {
  return TrackPlayer.skipToNext()
}

async function skipToPrevious(): Promise<void> {
  return TrackPlayer.skipToPrevious()
}

async function updateMetadataForTrack(trackId: string, metadata: TrackMetadata): Promise<void> {
  return TrackPlayer.updateMetadataForTrack(trackId, metadata)
}

// MARK: Playback API

async function reset(): Promise<void> {
  return TrackPlayer.reset()
}

async function play(): Promise<void> {
  return TrackPlayer.play()
}

async function playWithEarPiece(): Promise<void> {
  return TrackPlayer.playWithEarPiece()
}

async function pause(): Promise<void> {
  return TrackPlayer.pause()
}

async function stop(): Promise<void> {
  return TrackPlayer.stop()
}

async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position)
}

async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level)
}

async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate)
}

// MARK: - Getters

async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume()
}

async function getRate(): Promise<number> {
  return TrackPlayer.getRate()
}

async function getTrack(trackId: string): Promise<Track> {
  return TrackPlayer.getTrack(trackId)
}

async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue()
}

async function getCurrentTrack(): Promise<string> {
  return TrackPlayer.getCurrentTrack()
}

async function getDuration(): Promise<number> {
  return TrackPlayer.getDuration()
}

async function getBufferedPosition(): Promise<number> {
  return TrackPlayer.getBufferedPosition()
}

async function getPosition(): Promise<number> {
  return TrackPlayer.getPosition()
}

async function getState(): Promise<State> {
  return TrackPlayer.getState()
}

export * from './hooks'
export * from './interfaces'

export default {
  // MARK: - General API
  setupPlayer,
  destroy,
  updateOptions,
  registerPlaybackService,
  addEventListener,
  isServiceRunning: TrackPlayer.isServiceRunning,
  // MARK: - Queue API
  add,
  remove,
  removeUpcomingTracks,
  skip,
  skipToNext,
  skipToPrevious,
  updateMetadataForTrack,

  // MARK: Playback API
  reset,
  play,
  playWithEarPiece,
  pause,
  stop,
  seekTo,
  setVolume,
  setRate,

  // MARK: - Getters
  getVolume,
  getRate,
  getTrack,
  getQueue,
  getCurrentTrack,
  getDuration,
  getBufferedPosition,
  getPosition,
  getState,
}
