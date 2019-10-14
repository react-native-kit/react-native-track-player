import { NativeModules } from 'react-native'
const { TrackPlayerModule: TrackPlayer } = NativeModules

export enum IOSCategory {
  Playback = 'playback',
  PlayAndRecord = 'playAndRecord',
  MultiRoute = 'multiRoute',
  Ambient = 'ambient',
  SoloAmbient = 'soloAmbient',
  Record = 'record',
}

export enum IOSCategoryMode {
  Default = 'default',
  GameChat = 'gameChat',
  Measurement = 'measurement',
  MoviePlayback = 'moviePlayback',
  SpokenAudio = 'spokenAudio',
  VideoChat = 'videoChat',
  VideoRecording = 'videoRecording',
  VoiceChat = 'voiceChat',
  VoicePrompt = 'voicePrompt',
}

export enum IOSCategoryOptions {
  MixWithOthers = 'mixWithOthers',
  DuckOthers = 'duckOthers',
  InterruptSpokenAudioAndMixWithOthers = 'interruptSpokenAudioAndMixWithOthers',
  AllowBluetooth = 'allowBluetooth',
  AllowBluetoothA2DP = 'allowBluetoothA2DP',
  AllowAirPlay = 'allowAirPlay',
  DefaultToSpeaker = 'defaultToSpeaker',
}

export interface PlayerOptions {
  minBuffer?: number
  maxBuffer?: number
  playBuffer?: number
  maxCacheSize?: number
  iosCategory?: IOSCategory
  iosCategoryMode?: IOSCategoryMode
  iosCategoryOptions?: IOSCategoryOptions[]
  waitForBuffer?: boolean
}

export enum RatingType {
  Heart = TrackPlayer.RATING_HEART,
  ThumbsUpDown = TrackPlayer.RATING_THUMBS_UP_DOWN,
  ThreeStars = TrackPlayer.RATING_3_STARS,
  FourStars = TrackPlayer.RATING_4_STARS,
  FiveStars = TrackPlayer.RATING_5_STARS,
  Percentage = TrackPlayer.RATING_PERCENTAGE,
}

export interface FeedbackOptions {
  /** Marks wether the option should be marked as active or "done" */
  isActive: boolean

  /** The title to give the action (relevant for iOS) */
  title: string
}

export enum Capability {
  Play = TrackPlayer.CAPABILITY_PLAY,
  PlayFromId = TrackPlayer.CAPABILITY_PLAY_FROM_ID,
  PlayFromSearch = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH,
  Pause = TrackPlayer.CAPABILITY_PAUSE,
  Stop = TrackPlayer.CAPABILITY_STOP,
  SeekTo = TrackPlayer.CAPABILITY_SEEK_TO,
  Skip = TrackPlayer.CAPABILITY_SKIP,
  SkipToNext = TrackPlayer.CAPABILITY_SKIP_TO_NEXT,
  SkipToPrevious = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS,
  JumpForward = TrackPlayer.CAPABILITY_JUMP_FORWARD,
  JumpBackward = TrackPlayer.CAPABILITY_JUMP_BACKWARD,
  SetRating = TrackPlayer.CAPABILITY_SET_RATING,
  Like = TrackPlayer.CAPABILITY_LIKE,
  Dislike = TrackPlayer.CAPABILITY_DISLIKE,
  Bookmark = TrackPlayer.CAPABILITY_BOOKMARK,
}

export type ResourceObject = number

export interface MetadataOptions {
  ratingType?: RatingType
  jumpInterval?: number
  likeOptions?: FeedbackOptions
  dislikeOptions?: FeedbackOptions
  bookmarkOptions?: FeedbackOptions
  stopWithApp?: boolean

  capabilities?: Capability[]
  notificationCapabilities?: Capability[]
  compactCapabilities?: Capability[]

  icon?: ResourceObject
  playIcon?: ResourceObject
  pauseIcon?: ResourceObject
  stopIcon?: ResourceObject
  previousIcon?: ResourceObject
  nextIcon?: ResourceObject
  rewindIcon?: ResourceObject
  forwardIcon?: ResourceObject
  color?: number
}

export enum Event {
  PlaybackState = 'playback-state',
  PlaybackError = 'playback-error',
  PlaybackQueueEnded = 'playback-queue-ended',
  PlaybackTrackChanged = 'playback-track-changed',
  RemotePlay = 'remote-play',
  RemotePlayId = 'remote-play-id',
  RemotePlaySearch = 'remote-play-search',
  RemotePause = 'remote-pause',
  RemoteStop = 'remote-stop',
  RemoteSkip = 'remote-skip',
  RemoteNext = 'remote-next',
  RemotePrevious = 'remote-previous',
  RemoteJumpForward = 'remote-jump-forward',
  RemoteJumpBackward = 'remote-jump-backward',
  RemoteSeek = 'remote-seek',
  RemoteSetRating = 'remote-set-rating',
  RemoteDuck = 'remote-duck',
  RemoteLike = 'remote-like',
  RemoteDislike = 'remote-dislike',
  RemoteBookmark = 'remote-bookmark',
  HeadsetPluggedIn = 'headset-plugged-in',
  HeadsetPluggedOut = 'headset-plugged-out',
  BluetoothConnected = 'bluetooth-connected',
  BluetoothDisconnected = 'bluetooth-disconnected',

}

export enum TrackType {
  Default = 'default',
  Dash = 'dash',
  HLS = 'hls',
  SmoothStreaming = 'smoothstreaming',
}

export enum PitchAlgorithm {
  Linear = TrackPlayer.PITCH_ALGORITHM_LINEAR,
  Music = TrackPlayer.PITCH_ALGORITHM_MUSIC,
  Voice = TrackPlayer.PITCH_ALGORITHM_VOICE,
}

export enum State {
  None = TrackPlayer.STATE_NONE,
  Ready = TrackPlayer.STATE_READY,
  Playing = TrackPlayer.STATE_PLAYING,
  Paused = TrackPlayer.STATE_PAUSED,
  Stopped = TrackPlayer.STATE_STOPPED,
  Buffering = TrackPlayer.STATE_BUFFERING,
  Connecting = TrackPlayer.STATE_CONNECTING,
}

export interface TrackMetadata {
  duration?: number
  title: string
  artist: string
  album?: string
  description?: string
  genre?: string
  date?: string
  rating?: number | boolean
  artwork?: string | ResourceObject
}

export interface Track extends TrackMetadata {
  id: string
  url: string | ResourceObject
  type?: TrackType
  userAgent?: string
  contentType?: string
  pitchAlgorithm?: PitchAlgorithm
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: any
}
