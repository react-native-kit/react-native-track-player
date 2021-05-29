//
//  RemoteCommand.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 20/03/2018.
//

import Foundation
import MediaPlayer


public typealias RemoteCommandHandler = (MPRemoteCommandEvent) -> MPRemoteCommandHandlerStatus

public protocol RemoteCommandProtocol {
    associatedtype Command: MPRemoteCommand
    
    var id: String { get }
    var commandKeyPath: KeyPath<MPRemoteCommandCenter, Command> { get }
    var handlerKeyPath: KeyPath<RemoteCommandController, RemoteCommandHandler> { get }
}

public struct PlayBackCommand: RemoteCommandProtocol {
    
    public static let play = PlayBackCommand(id: "Play", commandKeyPath: \MPRemoteCommandCenter.playCommand, handlerKeyPath: \RemoteCommandController.handlePlayCommand)
    
    public static let pause = PlayBackCommand(id: "Pause", commandKeyPath: \MPRemoteCommandCenter.pauseCommand, handlerKeyPath: \RemoteCommandController.handlePauseCommand)
    
    public static let stop = PlayBackCommand(id: "Stop", commandKeyPath: \MPRemoteCommandCenter.stopCommand, handlerKeyPath: \RemoteCommandController.handleStopCommand)
    
    public static let togglePlayPause = PlayBackCommand(id: "TogglePlayPause", commandKeyPath: \MPRemoteCommandCenter.togglePlayPauseCommand, handlerKeyPath: \RemoteCommandController.handleTogglePlayPauseCommand)
    
    public static let nextTrack = PlayBackCommand(id: "NextTrackCommand", commandKeyPath: \MPRemoteCommandCenter.nextTrackCommand, handlerKeyPath: \RemoteCommandController.handleNextTrackCommand)
    
    public static let previousTrack = PlayBackCommand(id: "PreviousTrack", commandKeyPath: \MPRemoteCommandCenter.previousTrackCommand, handlerKeyPath: \RemoteCommandController.handlePreviousTrackCommand)
    
    
    public typealias Command = MPRemoteCommand
    
    public let id: String
    
    public var commandKeyPath: KeyPath<MPRemoteCommandCenter, MPRemoteCommand>
    
    public var handlerKeyPath: KeyPath<RemoteCommandController, RemoteCommandHandler>
    
}

public struct ChangePlaybackPositionCommand: RemoteCommandProtocol {
    
    public static let changePlaybackPosition = ChangePlaybackPositionCommand(id: "ChangePlaybackPosition", commandKeyPath: \MPRemoteCommandCenter.changePlaybackPositionCommand, handlerKeyPath: \RemoteCommandController.handleChangePlaybackPositionCommand)
    
    public typealias Command = MPChangePlaybackPositionCommand
    
    public let id: String
    
    public var commandKeyPath: KeyPath<MPRemoteCommandCenter, MPChangePlaybackPositionCommand>
    
    public var handlerKeyPath: KeyPath<RemoteCommandController, RemoteCommandHandler>
    
}

public struct SkipIntervalCommand: RemoteCommandProtocol {
    
    public static let skipForward = SkipIntervalCommand(id: "SkipForward", commandKeyPath: \MPRemoteCommandCenter.skipForwardCommand, handlerKeyPath: \RemoteCommandController.handleSkipForwardCommand)
    
    public static let skipBackward = SkipIntervalCommand(id: "SkipBackward", commandKeyPath: \MPRemoteCommandCenter.skipBackwardCommand, handlerKeyPath: \RemoteCommandController.handleSkipBackwardCommand)

    public typealias Command = MPSkipIntervalCommand
    
    public let id: String
    
    public var commandKeyPath: KeyPath<MPRemoteCommandCenter, MPSkipIntervalCommand>
    
    public var handlerKeyPath: KeyPath<RemoteCommandController, RemoteCommandHandler>
    
    func set(preferredIntervals: [NSNumber]) -> SkipIntervalCommand {
        MPRemoteCommandCenter.shared()[keyPath: commandKeyPath].preferredIntervals = preferredIntervals
        return self
    }
    
}

public struct FeedbackCommand: RemoteCommandProtocol {
    
    public static let like = FeedbackCommand(id: "Like", commandKeyPath: \MPRemoteCommandCenter.likeCommand, handlerKeyPath: \RemoteCommandController.handleLikeCommand)
    
    public static let dislike = FeedbackCommand(id: "Dislike", commandKeyPath: \MPRemoteCommandCenter.dislikeCommand, handlerKeyPath: \RemoteCommandController.handleDislikeCommand)
    
    public static let bookmark = FeedbackCommand(id: "Bookmark", commandKeyPath: \MPRemoteCommandCenter.bookmarkCommand, handlerKeyPath: \RemoteCommandController.handleBookmarkCommand)
    
    public typealias Command = MPFeedbackCommand
    
    public let id: String
    
    public var commandKeyPath: KeyPath<MPRemoteCommandCenter, MPFeedbackCommand>
    
    public var handlerKeyPath: KeyPath<RemoteCommandController, RemoteCommandHandler>
    
    func set(isActive: Bool, localizedTitle: String, localizedShortTitle: String) -> FeedbackCommand {
        MPRemoteCommandCenter.shared()[keyPath: commandKeyPath].isActive = isActive
        MPRemoteCommandCenter.shared()[keyPath: commandKeyPath].localizedTitle = localizedTitle
        MPRemoteCommandCenter.shared()[keyPath: commandKeyPath].localizedShortTitle = localizedShortTitle
        return self
    }
}

public enum RemoteCommand : Equatable {

    case play
    
    case pause
    
    case stop
    
    case togglePlayPause
    
    case next
    
    case previous
    
    case changePlaybackPosition
    
    case skipForward(preferredIntervals: [NSNumber])
    
    case skipBackward(preferredIntervals: [NSNumber])
    
    case like(isActive: Bool, localizedTitle: String, localizedShortTitle: String)
    
    case dislike(isActive: Bool, localizedTitle: String, localizedShortTitle: String)
    
    case bookmark(isActive: Bool, localizedTitle: String, localizedShortTitle: String)

    var `case`: Case {
      switch self {
          case .play: return .play
          case .pause: return .pause
          case .stop: return .stop
          case .togglePlayPause: return .togglePlayPause
          case .next: return .next
          case .previous: return .previous
          case .changePlaybackPosition: return .changePlaybackPosition
          case .skipForward: return .skipForward
          case .skipBackward: return .skipBackward
          case .like: return .like
          case .dislike: return .dislike
          case .bookmark: return .bookmark
      }
    }

    enum Case {
        case play
        case pause
        case stop
        case togglePlayPause
        case next
        case previous
        case changePlaybackPosition
        case skipForward
        case skipBackward
        case like
        case dislike
        case bookmark
    }

    public static func ==(lhs: RemoteCommand, rhs: RemoteCommand) -> Bool {
        return lhs.case == rhs.case
    }
    
    /**
     All values in an array for convenience.
     Don't use for associated values.
     */
    static func all() -> [RemoteCommand] {
        return [
            .play,
            .pause,
            .stop,
            .togglePlayPause,
            .next,
            .previous,
            .changePlaybackPosition,
            .skipForward(preferredIntervals: []),
            .skipBackward(preferredIntervals: []),
            .like(isActive: false, localizedTitle: "", localizedShortTitle: ""),
            .dislike(isActive: false, localizedTitle: "", localizedShortTitle: ""),
            .bookmark(isActive: false, localizedTitle: "", localizedShortTitle: "")
        ]
    }
    
}
