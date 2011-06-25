package de.sciss.gui

import j.{PeakMeter => JPeakMeter, PeakMeterLike}
import collection.immutable.{IndexedSeq => IIdxSeq}
import swing.{Alignment, Orientable, Orientation, Component}

class PeakMeter extends Component with PeakMeterLike {
   override lazy val peer: JPeakMeter = new JPeakMeter with SuperMixin

   def clearHold() { peer.clearHold() }
   def clearMeter() { peer.clearMeter() }
   def dispose() { peer.dispose() }

   def channel( ch: Int ) = peer.channel( ch )
   def numChannels = peer.numChannels
   def numChannels_=( n: Int ) { peer.numChannels = n }

   def holdDuration_=( millis: Int ) { peer.holdDuration = millis }
   def holdDuration = peer.holdDuration

   def rmsPainted_=( b: Boolean ) { peer.rmsPainted = b }
   def rmsPainted = peer.rmsPainted

   def holdPainted_=( b: Boolean ) { peer.holdPainted = b }
   def holdPainted = peer.holdPainted

   def orientation_=( value: Orientation.Value ) { peer.orientation = value.id }
   def orientation: Orientation.Value = Orientation( peer.orientation )

//   def orientation_=( orient: Int ) { peer.orientation = orient }
//   def orientation = 0

   def ticks_=( num: Int ) { peer.ticks = num }
   def ticks = peer.ticks

//   def update( peak: Float, rms: Float, time: Long ) = peer.update( peak, rms, time )
   def update( peakRMSPairs: IIdxSeq[ Float ], offset: Int, time: Long ) = peer.update( peakRMSPairs, offset, time )

   def hasCaption = peer.hasCaption
   def hasCaption_=( b: Boolean ) { peer.hasCaption = b }

   def captionLabels = peer.captionLabels
   def captionLabels_=( b: Boolean ) { peer.captionLabels = b }

   def captionVisible = peer.captionVisible
   def captionVisible_=( b: Boolean ) { peer.captionVisible = b }

   def captionPosition_=( value: Alignment.Value ) { peer.captionPosition = value.id }
   def captionPosition: Alignment.Value = Alignment( peer.captionPosition )

   def borderVisible = peer.borderVisible
   def borderVisible_=( b: Boolean ) { peer.borderVisible = b }
}