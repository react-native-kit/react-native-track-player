//
//  Track.swift
//  RNTrackPlayer
//
//  Created by David Chavez on 12.08.17.
//  Copyright © 2017 David Chavez. All rights reserved.
//

import Foundation
import MediaPlayer
import AVFoundation

class Track: NSObject, AudioItem, InitialTiming, TimePitching, Authorizing {
    let id: String
    let url: MediaURL
    
    @objc var title: String
    @objc var artist: String
    
    var date: String?
    var desc: String?
    var genre: String?
    var duration: Double?
    var skipped: Bool = false
    var artworkURL: MediaURL?
    var initialTime: TimeInterval = 0.0
    let headers: [String: Any]?
    let pitchAlgorithm: String?
    
    @objc var album: String?
    @objc var artwork: MPMediaItemArtwork?
    
    private var originalObject: [String: Any]
    
    init?(dictionary: [String: Any]) {
        guard let id = dictionary["id"] as? String,
            let title = dictionary["title"] as? String,
            let artist = dictionary["artist"] as? String,
            let url = MediaURL(object: dictionary["url"])
            else { return nil }
        
        self.id = id
        self.url = url
        self.title = title
        self.artist = artist
        
        self.date = dictionary["date"] as? String
        self.album = dictionary["album"] as? String
        self.genre = dictionary["genre"] as? String
        self.desc = dictionary["description"] as? String
        self.duration = dictionary["duration"] as? Double
        self.headers = dictionary["headers"] as? [String: Any]
        self.artworkURL = MediaURL(object: dictionary["artwork"])
        let initialTime = dictionary["initialTime"] as? Double
        if let x = initialTime {
            self.initialTime = x
        }
        self.pitchAlgorithm = dictionary["pitchAlgorithm"] as? String
        
        self.originalObject = dictionary
    }
    
    
    // MARK: - Public Interface
    
    func toObject() -> [String: Any] {
        return originalObject
    }
    
    func updateMetadata(dictionary: [String: Any]) {
        self.title = (dictionary["title"] as? String) ?? self.title
        self.artist = (dictionary["artist"] as? String) ?? self.artist
        
        self.date = dictionary["date"] as? String
        self.album = dictionary["album"] as? String
        self.genre = dictionary["genre"] as? String
        self.desc = dictionary["description"] as? String
        self.duration = dictionary["duration"] as? Double
        self.artworkURL = MediaURL(object: dictionary["artwork"])
        
        self.originalObject = self.originalObject.merging(dictionary) { (_, new) in new }
    }
    
    // MARK: - AudioItem Protocol
    
    func getSourceUrl() -> String {
        return url.value.absoluteString
    }
    
    func getArtist() -> String? {
        return artist
    }
    
    func getTitle() -> String? {
        return title
    }
    
    func getAlbumTitle() -> String? {
        return album
    }
    
    func getSourceType() -> SourceType {
        return url.isLocal ? .file : .stream
    }
    
    func getArtwork(_ handler: @escaping (UIImage?) -> Void) {
        if let artworkURL = artworkURL?.value {
            URLSession.shared.dataTask(with: artworkURL, completionHandler: { (data, _, error) in
                if let data = data, let artwork = UIImage(data: data), error == nil {
                    handler(artwork)
                }
                
                handler(nil)
            }).resume()
        }
        
        handler(nil)
    }

    // MARK: - TimePitching Protocol

    func getPitchAlgorithmType() -> AVAudioTimePitchAlgorithm {
        if let pitchAlgorithm = pitchAlgorithm {
            switch pitchAlgorithm {
            case PitchAlgorithm.linear.rawValue:
                return .varispeed
            case PitchAlgorithm.music.rawValue:
                return .spectral
            case PitchAlgorithm.voice.rawValue:
                return .timeDomain
            default:
                return .lowQualityZeroLatency
            }
        }
        
        return .lowQualityZeroLatency
    }
    
    // MARK: - Authorizing Protocol
    
    func getHeaders() -> [String : Any] {
        return headers ?? [:]
    }
    
    // MARK: - InitialTiming Protocol
    func getInitialTime() -> TimeInterval {
        return initialTime
    }
}
