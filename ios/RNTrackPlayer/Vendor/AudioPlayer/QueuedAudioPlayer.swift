//
//  QueuedAudioPlayer.swift
//  SwiftAudio
//
//  Created by Jørgen Henrichsen on 24/03/2018.
//

import Foundation
import MediaPlayer

/**
 An audio player that can keep track of a queue of AudioItems.
 */
public class QueuedAudioPlayer: AudioPlayer {
    
    let queueManager: QueueManager = QueueManager<AudioItem>()
    
    /**
     Set wether the player should automatically play the next song when a song is finished.
     Default is `true`.
     */
    public var automaticallyPlayNextSong: Bool = true
    
    public override var currentItem: AudioItem? {
        return queueManager.current
    }
    
    /**
     The index of the current item.
     */
    public var currentIndex: Int {
        return queueManager.currentIndex
    }
    
     /**
     Stops the player and clears the queue.
     */
    public override func stop() {
        super.stop()
    }
    
    override func reset() {
        queueManager.clearQueue()
    }
    
    /**
     All items currently in the queue.
     */
    public var items: [AudioItem] {
        return queueManager.items
    }
    
    /**
     The previous items held by the queue.
     */
    public var previousItems: [AudioItem] {
        return queueManager.previousItems
    }
    
    /**
     The upcoming items in the queue.
     */
    public var nextItems: [AudioItem] {
        return queueManager.nextItems
    }
    
    /**
     Will replace the current item with a new one and load it into the player.
     
     - parameter item: The AudioItem to replace the current item.
     - throws: APError.LoadError
     */
    public override func load(item: AudioItem, playWhenReady: Bool) throws {
        try super.load(item: item, playWhenReady: playWhenReady)
        queueManager.replaceCurrentItem(with: item)
    }
    
    /**
     Add a single item to the queue.
     
     - parameter item: The item to add.
     - parameter playWhenReady: If the AudioPlayer has no item loaded, it will load the `item`. If this is `true` it will automatically start playback. Default is `true`.
     - throws: `APError`
     */
    public func add(item: AudioItem, playWhenReady: Bool = true) throws {
        if currentItem == nil {
            queueManager.addItem(item)
            if playWhenReady == true { 
             try self.load(item: item, playWhenReady: playWhenReady)
            }
        }
        else {
            queueManager.addItem(item)
        }
    }
    
    /**
     Add items to the queue.
     
     - parameter items: The items to add to the queue.
     - parameter playWhenReady: If the AudioPlayer has no item loaded, it will load the first item in the list. If this is `true` it will automatically start playback. Default is `true`.
     - throws: `APError`
     */
    public func add(items: [AudioItem], playWhenReady: Bool = true) throws {
        if currentItem == nil {
            queueManager.addItems(items)
            if playWhenReady == true { 
             try self.load(item: currentItem!, playWhenReady: playWhenReady)
            }
        }
        else {
            queueManager.addItems(items)
        }
    }
    
    public func add(items: [AudioItem], at index: Int) throws {
        try queueManager.addItems(items, at: index)
    }
    
    /**
     Step to the next item in the queue.
     
     - throws: `APError`
     */
    public func next() throws {
        event.playbackEnd.emit(data: .skippedToNext)
        delegate?.audioPlayer(itemPlaybackEndedWithReason: .skippedToNext)
        let nextItem = try queueManager.next()
        try self.load(item: nextItem, playWhenReady: true)
    }
    
    /**
     Step to the previous item in the queue.
     */
    public func previous() throws {
        event.playbackEnd.emit(data: .skippedToPrevious)
        delegate?.audioPlayer(itemPlaybackEndedWithReason: .skippedToPrevious)
        let previousItem = try queueManager.previous()
        try self.load(item: previousItem, playWhenReady: true)
    }
    
    /**
     Remove an item from the queue.
     
     - parameter index: The index of the item to remove.
     - throws: `APError.QueueError`
     */
    public func removeItem(at index: Int) throws {
        try queueManager.removeItem(at: index)
    }
    
    /**
     Jump to a certain item in the queue.
     
     - parameter index: The index of the item to jump to.
     - parameter playWhenReady: Wether the item should start playing when ready. Default is `true`.
     - throws: `APError`
     */
    public func jumpToItem(atIndex index: Int, playWhenReady: Bool = true) throws {
        event.playbackEnd.emit(data: .jumpedToIndex)
        delegate?.audioPlayer(itemPlaybackEndedWithReason: .jumpedToIndex)
        let item = try queueManager.jump(to: index)
        try self.load(item: item, playWhenReady: playWhenReady)
    }
    
    /**
     Move an item in the queue from one position to another.
     
     - parameter fromIndex: The index of the item to move.
     - parameter toIndex: The index to move the item to.
     - throws: `APError.QueueError`
     */
    func moveItem(fromIndex: Int, toIndex: Int) throws {
        try queueManager.moveItem(fromIndex: fromIndex, toIndex: toIndex)
    }
    
    /**
     Remove all upcoming items, those returned by `next()`
     */
    public func removeUpcomingItems() {
        queueManager.removeUpcomingItems()
    }
    
    /**
     Remove all previous items, those returned by `previous()`
     */
    public func removePreviousItems() {
        queueManager.removePreviousItems()
    }
    
    // MARK: - AVPlayerWrapperDelegate
    
    override func AVWrapperItemDidPlayToEndTime() {
        super.AVWrapperItemDidPlayToEndTime()
        if automaticallyPlayNextSong {
            try? self.next()
        }
    }
    
}
