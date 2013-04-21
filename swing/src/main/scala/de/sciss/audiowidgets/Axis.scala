/*
 *  Axis.scala
 *  (AudioWidgets)
 *
 *  Copyright (c) 2011-2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.audiowidgets

import j.{Axis => JAxis}
import swing.Component

class Axis extends Component with AxisLike {
  override lazy val peer: JAxis = new JAxis with SuperMixin

  def fixedBounds = peer.fixedBounds
  def fixedBounds_=(b: Boolean) {
    peer.fixedBounds = b
  }

  // stupid translations...
  def format: AxisFormat = peer.format

  // stupid translations...
  def format_=(f: AxisFormat) {
    peer.format = f
  }

  def inverted = peer.inverted
  def inverted_=(b: Boolean) {
    peer.inverted = b
  }

  def maximum = peer.maximum
  def maximum_=(value: Double) {
    peer.maximum = value
  }

  def minimum = peer.minimum
  def minimum_=(value: Double) {
    peer.minimum = value
  }
}