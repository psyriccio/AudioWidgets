/*
 *  LCDPanel.java
 *  (AudioWidgets)
 *
 *  Copyright (c) 2011-2016 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.audiowidgets
package j

import javax.swing.{UIManager, JPanel}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.awt.{Rectangle, RenderingHints, Graphics2D, LinearGradientPaint, Insets, Color, Graphics}

/**
 * Unfinished!
 */
class LCDPanel extends JPanel {
  private final val gradInnerColr     = new Array[Color](5)
  private final val gradInnerFrac     = Array(0f, -1f, 0.5f, -1f, 1f)
  private final val gradOuterLColr    = new Array[Color](3)
  private final val gradOuterLFrac    = Array(0f, -1f, 1f)
  private final val gradOuterRColr    = new Array[Color](3)
  private final val gradOuterRFrac    = Array(0f, -1f, 1f)
  private final val gradInnerLColr    = new Array[Color](2)
  private final val gradInnerLFrac    = Array(0f, 1f)
  private final val gradInnerRColr    = new Array[Color](2)
  private final val gradInnerRFrac    = Array(0f, 0.5f)
  private final var colrTop: Color    = null
  private final var colrTopSh: Color  = null
  private final val colrBot           =
    if (Util.isDarkSkin) new Color(0x3F, 0x3F, 0x3F, 0x7F) else new Color(0xFF, 0xFF, 0xFF, 0x7F)
  private final var colrBotSh: Color  = null
  private final var recentHeight      = -1
  private final val in                = new Insets(0, 0, 0, 0)
  private final val rClip             = new Rectangle()
  private final var gradInner : LinearGradientPaint = null
  private final var gradOuterL: LinearGradientPaint = null
  private final var gradOuterR: LinearGradientPaint = null
  private final var gradInnerL: LinearGradientPaint = null
  private final var gradInnerR: LinearGradientPaint = null
  private final val inH = 2
  private final val inV = 0 // 1
  //   private val strkSh = new BasicStroke( 2f )

  setBackground(LCDColors.background)
  recalculateColors()

  addPropertyChangeListener("background", new PropertyChangeListener {
    def propertyChange(e: PropertyChangeEvent): Unit = {
      recalculateColors()
      repaint()
    }
  })

  override def getMaximumSize = getPreferredSize
  override def getMinimumSize = getPreferredSize

  override def getInsets: Insets = getInsets(new Insets(0, 0, 0, 0))
  override def getInsets(insets: Insets): Insets = {
    super.getInsets(insets)
    insets.top    += inV
    insets.left   += inH
    insets.bottom += inV
    insets.right  += inH
    insets
  }

  //   private def mixColor( hueOffset: Float, satFactor: Float, briFactor: Float ) =
  //      Color.getHSBColor( hue + hueOffset,
  //                         math.max( 0f, math.min( 1f, sat * satFactor )),
  //                         math.max( 0f, math.min( 1f, bri * briFactor )))


  //   private def changeAlpha( c: Color, alpha: Float ) =
  //      new Color( c.getRed, c.getGreen, c.getBlue, (alpha * 255).toInt )

  private def recalculateColors(): Unit = {
    val c   = getBackground
    val arr = Color.RGBtoHSB(c.getRed, c.getGreen, c.getBlue, null)
    val hue = arr(0)
    val sat = arr(1)
    val bri = arr(2)

    def mixColor2(hueOffset: Float, satOffset: Float, briOffset: Float) =
      Color.getHSBColor(hue + hueOffset,
        math.max(0f, math.min(1f, sat + satOffset)),
        math.max(0f, math.min(1f, bri + briOffset)))

    def mixColor3(hueOffset: Float, satOffset: Float, briWeight: Float, c1: Color, c2: Color) = {
      val arr = Color.RGBtoHSB(c1.getRed, c1.getGreen, c1.getBlue, null)
      val b1  = arr(2)
      Color.RGBtoHSB(c2.getRed, c2.getGreen, c2.getBlue, arr)
      val b2  = arr(2)
      Color.getHSBColor(hue + hueOffset,
        math.max(0f, math.min(1f, sat + satOffset)),
        (1f - briWeight) * b1 + briWeight * b2)
    }

    //      gradInnerColrs( 0 )  = mixColor( 0.006f, 0.737f, 1.019f )
    //      gradInnerColrs( 1 )  = mixColor( 0f,     0.737f, 1.043f )
    //      gradInnerColrs( 2 )  = mixColor( 0f,     0.895f, 1.019f )
    //      gradInnerColrs( 3 )  = mixColor( 0f,     1.105f, 0.981f )
    //      gradInnerColrs( 4 )  = mixColor( 0f,     0.684f, 1.056f )
    //      colrTop              = mixColor( 0.006f, 0.632f, 0.398f )
    //      colrTopSh            = mixColor( 0f,     1.737f, 0.795f )
    //      colrBotSh            = mixColor( 0f,     0.684f, 0.932f )
    gradInnerColr(0) = mixColor2(0.006f, -0.05f, 0.015f)
    gradInnerColr(1) = mixColor2(0f, -0.05f, 0.035f)
    gradInnerColr(2) = mixColor2(0f, -0.02f, 0.015f)
    gradInnerColr(3) = mixColor2(0f, 0.02f, -0.015f)
    gradInnerColr(4) = mixColor2(0f, -0.06f, 0.045f)
    colrTop = mixColor2(0.006f, -0.07f, -0.485f)
    //      colrTopSh            = mixColor2( 0f,      0.14f, -0.165f )
    colrTopSh = mixColor3(0f, 0.14f, 0.64f, colrTop, gradInnerColr(0))
    colrBotSh = mixColor2(0f, -0.06f, -0.055f)

    gradOuterLColr(0) = colrTop
    val outerLColr = mixColor2(-0.006f, -0.08f, -0.255f)
    gradOuterLColr(1) = outerLColr
    gradOuterLColr(2) = colrBot // changeAlpha( outerLColr, 0f )

    gradOuterRColr(0) = colrTop
    val outerRColr = mixColor2(0.003f, -0.08f, -0.205f)
    gradOuterRColr(1) = outerRColr
    gradOuterRColr(2) = colrBot // changeAlpha( outerRColr, 0f )

    gradInnerLColr(0) = colrTopSh
    gradInnerLColr(1) = mixColor2(0.003f, -0.06f, 0.015f)
    gradInnerRColr(0) = colrTopSh
    gradInnerRColr(1) = mixColor2(0.003f, 0.02f, -0.025f)
  }

  private def recalculateGradients(h: Int): Unit = {
    val hi            = math.max(1, h - 4)
    val fi1           = math.min(0.499f, 1f / hi)
    val fi3           = math.min(0.999f, 0.5f + 1f / hi)
    gradInnerFrac(1)  = fi1
    gradInnerFrac(3)  = fi3
    gradInner         = new LinearGradientPaint(0f, 2f, 0f, (2 + hi).toFloat, gradInnerFrac, gradInnerColr)
    recentHeight      = h

    val ho            = math.max(1, h)
    val fo1           = math.max(0.001f, (h - 2).toFloat / ho)
    gradOuterLFrac(1) = fo1
    gradOuterRFrac(1) = fo1
    gradOuterL        = new LinearGradientPaint(0f, 0f, 0f, h.toFloat, gradOuterLFrac, gradOuterLColr)
    gradOuterR        = new LinearGradientPaint(0f, 0f, 0f, h.toFloat, gradOuterRFrac, gradOuterRColr)

    val hi2           = math.max(1, h - 2)
    gradInnerL        = new LinearGradientPaint(0f, 1f, 0f, (1 + hi2).toFloat, gradInnerLFrac, gradInnerLColr)
    gradInnerR        = new LinearGradientPaint(0f, 1f, 0f, (1 + hi2).toFloat, gradInnerRFrac, gradInnerRColr)
  }

  override def paintComponent(g: Graphics): Unit = {
    getInsets(in)
    in.top    -= inV
    in.left   -= inH
    in.bottom -= inV
    in.right  -= inH
    val h   = getHeight - (in.top + in.bottom)
    val hh  = (h >> 1) + 1
    val w   = getWidth - (in.left + in.right)
    val w1  = math.max(0, w - 2)
    val x2  = math.max(0, w - 4)
    val x3  = math.max(0, w - 3)
    if (h != recentHeight) recalculateGradients(h)
    val g2 = g.asInstanceOf[Graphics2D]

    val atOrig = g2.getTransform
    g2.translate(in.left, in.top)
    //      g2.setPaint( gradInner )
    //      g2.fillRect( 1, 2, w1, h - 4 )
    ////      g2.fillRect( 1, 1, w1, h - 2 )

    val clpOrig = g2.getClip
    g2.getClipBounds(rClip)
    // avoid drawing antialiased rounded gradients, if possible :)
    val drawL = rClip.x < 3
    val drawR = rClip.x + rClip.width > x3
    val aaOrig = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    //println( "L? " + drawL + " - R? " + drawR )

    if (drawL) {
      g2.clipRect(0, 0, 3, h)
      g2.setPaint(gradOuterL)
      //      g2.drawRoundRect( 0, 0, 6, h, 4, 4 )
      g2.fillRoundRect(0, 0, 6, h, 4, 4)
      g2.setClip(clpOrig)
    }
    if (drawR) {
      g2.clipRect(w - 3, 0, 3, h)
      g2.setPaint(gradOuterR)
      //      g2.drawRoundRect( w - 7, 0, 6, h, 4, 4 )
      g2.fillRoundRect(w - 6, 0, 6, h, 4, 4)
      g2.setClip(clpOrig)
    }

    g2.setPaint(gradInner)
    g2.fillRect(1, 2, w1, h - 4)

    if (drawL) {
      g2.clipRect(0, 0, 3, h - 2)
      g2.setPaint(gradInnerL)
      g2.drawRoundRect(1, 1, 6, h, 4, 4)
      g2.setClip(clpOrig)
    }
    if (drawR) {
      g2.clipRect(w - 3, 0, 3, hh)
      g2.setPaint(gradInnerR)
      g2.drawRoundRect(w - 8, 1, 6, h, 4, 4)
      g2.setClip(clpOrig)
    }

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOrig)

    g2.setColor(colrTop)
    g2.drawLine(3, 0, x2, 0)
    g2.setColor(colrTopSh)
    g2.drawLine(3, 1, x2, 1)
    g2.setColor(colrBotSh)
    g2.drawLine(2, h - 2, x3, h - 2)
    g2.setColor(colrBot)
    //      g2.drawLine( 2, h - 1, x3, h - 1 )
    g2.drawLine(1, h - 1, w1, h - 1)

    g2.setTransform(atOrig)
  }
}