/*
 *  Transport.java
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

package de.sciss.audiowidgets.j

import java.awt.event.ActionEvent
import java.awt.geom.{AffineTransform, Area, Ellipse2D, GeneralPath, Rectangle2D, RoundRectangle2D}
import java.awt.{BasicStroke, Color, Component, Graphics, Graphics2D, LinearGradientPaint, Paint, RenderingHints, Shape}
import javax.swing.{AbstractAction, AbstractButton, Box, BoxLayout, Icon, JButton, JComponent}

import de.sciss.audiowidgets.{ShapeIcon, Util}

import scala.collection.immutable.{IndexedSeq => Vec}

trait TransportCompanion {
  // ---- abstract types and methods ----

  type ComponentType
  type AbstractButtonType <: ComponentType
  type Action <: ActionLike

  protected def makeAction(icons: (Icon, Icon, Icon, Icon), element: Element, scale: Float, fun: => Unit): Action

  def defaultColorScheme: ColorScheme = if (Util.isDarkSkin) LightScheme else DarkScheme

  def makeButtonStrip(actions: Seq[ActionElement], scale: Float = 0.8f,
                      scheme: ColorScheme = defaultColorScheme): ComponentType with ButtonStrip

  // ---- implemented ----

  sealed trait ColorScheme {
    def shadowPaint: Paint
    def outlinePaint: Paint
    def fillPaint(scale: Float): Paint
  }

  case object LightScheme extends ColorScheme {
    def fillPaint(scale: Float): Paint = new LinearGradientPaint(0f, 0f, 0f, scale * 19f, Array(0f, 0.25f, 1f),
      Array(new Color(0xA8, 0xA8, 0xA8), new Color(0xBF, 0xBF, 0xBF), new Color(0xB0, 0xB0, 0xB0))
    )

    val shadowPaint : Paint = new Color(0, 0, 0, 0x7F)
    val outlinePaint: Paint = new Color(0, 0, 0, 0xC0)
  }

  case object DarkScheme extends ColorScheme {
    def fillPaint(scale: Float): Paint = new LinearGradientPaint(0f, 0f, 0f, scale * 19f, Array(0f, 0.25f, 1f),
      Array(new Color(0x28, 0x28, 0x28), new Color(0x00, 0x00, 0x00), new Color(0x20, 0x20, 0x20))
    )

    val shadowPaint : Paint = new Color(0xFF, 0xFF, 0xFF, 0x7F)
    val outlinePaint: Paint = Color.white
  }

  protected final class IconImpl(val element: Element, val scale: Float, scheme: ColorScheme)
    extends Icon {

    private val inShape: Shape = element.shape(scale)
    private val outShape: Shape = calcOutShape(inShape)

    def getIconWidth  = math.ceil(24 * scale).toInt
    def getIconHeight = math.ceil(22 * scale).toInt

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2 = g.asInstanceOf[Graphics2D]
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING  , RenderingHints.VALUE_ANTIALIAS_ON)
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE )
      paintImpl(g2, inShape, outShape, x, y, scale, scheme)
    }
  }

  sealed trait Element {
    final def icon(scale: Float = 1f, colorScheme: ColorScheme = DarkScheme): Icon =
      new IconImpl(this, scale, colorScheme)

    /** Icons: (normal, selected, pressed, disabled). */
    final def icons(scale: Float = 1f, colorScheme: ColorScheme = DarkScheme): (Icon, Icon, Icon, Icon) = {
      val shp           = shape(scale)
      val isDark        = Util.isDarkSkin
      val w             = math.ceil(24 * scale).toInt
      val h             = math.ceil(22 * scale).toInt
      // val shadow        = if (isDark) new Color(0x00, 0x00, 0x00, 0x7F) else new Color(0xFF, 0xFF, 0xFF, 0x7F)
      val shadow        = colorScheme.shadowPaint
      // val iconNorm      = new ShapeIcon(shp, if (isDark) new Color(200 , 200 , 200      ) else new Color(32  , 32  , 32      ), shadow, w, h)
      val iconNorm      = new ShapeIcon(shp, colorScheme.fillPaint(scale), shadow, w, h)
      val iconSel       = new ShapeIcon(shp, if (isDark) new Color(0x5E, 0x97, 0xFF     ) else new Color(0x3D, 0x3D, 0xB6    ), shadow, w, h)
      val iconPressed   = new ShapeIcon(shp, if (isDark) new Color(180 , 180 , 180, 0x7F) else new Color(40  , 40  , 40, 0x7F), shadow, w, h)
      val iconDisabled  = iconPressed

      (iconNorm, iconSel, iconPressed, iconDisabled)
    }

    final def apply(fun: => Unit): ActionElement = {
      new ActionElementImpl(this, fun)
    }

    def defaultXOffset: Float
    def defaultYOffset: Float

    def shape(scale: Float = 1f, xoff: Float = defaultXOffset, yoff: Float = defaultYOffset): Shape
  }

  sealed trait ActionElement {
    def element: Element

    def apply(scale: Float = 1f, colorScheme: ColorScheme = DarkScheme): Action
  }

  private final class ActionElementImpl(val element: Element, fun: => Unit)
    extends ActionElement {
    def apply(scale: Float, colorScheme: ColorScheme): Action = {
      val icons = element.icons(scale, colorScheme)
      makeAction(icons, element, scale, fun)
    }
  }

  private val strk = new BasicStroke(1f)
  private val shadowYOff = 1f

  trait ActionLike /* extends javax.swing.Action */ {
    def icon: Icon
    def icons: (Icon, Icon, Icon, Icon)
    def element: Element
    def scale: Float
  }

  def paint(g: Graphics2D, iconType: Element, x: Float = 0f, y: Float = 0f, scale: Float = 1f,
            colorScheme: ColorScheme = DarkScheme): Unit = {
    val x1 = iconType.defaultXOffset * scale
    val y1 = iconType.defaultYOffset * scale
    //         val x1 = x
    //         val y1 = y
    val inShape = iconType.shape(scale, x1, y1)
    val outShape = calcOutShape(inShape)
    paintImpl(g, inShape, outShape, x - x1, y - y1, scale, colorScheme)
  }

  private def calcOutShape(inShape: Shape): Shape = {
    val out = new Area(strk.createStrokedShape(inShape))
    out.add(new Area(inShape))
    out
  }

  private def paintImpl(g2: Graphics2D, inShape: Shape, outShape: Shape, x: Float, y: Float, scale: Float, 
                        scheme: ColorScheme): Unit = {
    val atOrig = g2.getTransform
    g2.translate(x + 1, y + 1 + shadowYOff)
    g2.setPaint(scheme.shadowPaint)
    g2.fill(outShape)
    g2.translate(0, -shadowYOff)
    g2.setPaint(scheme.outlinePaint)
    g2.fill(outShape)
    g2.setPaint(scheme.fillPaint(scale))
    g2.fill(inShape)
    g2.setTransform(atOrig)
  }

  case object Play extends Element {
    val defaultXOffset = 4f
    val defaultYOffset = 0f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val gp = new GeneralPath()
      gp.moveTo(xoff, yoff)
      gp.lineTo(xoff + scale * 15f, yoff + scale * 10f)
      gp.lineTo(xoff, yoff + scale * 20f)
      gp.closePath()
      gp
    }
  }

  case object Stop extends Element {
    val defaultXOffset = 3f
    val defaultYOffset = 2f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape =
      new Rectangle2D.Float(xoff, yoff, scale * 16f, scale * 16f)
  }

  case object Pause extends Element {
    val defaultXOffset = 3f
    val defaultYOffset = 2f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val res = new Area(new Rectangle2D.Float(xoff, yoff, scale * 6f, scale * 16f))
      res.add(new Area(new Rectangle2D.Float(xoff + scale * 10f, yoff, scale * 6f, scale * 16f)))
      res
    }
  }

  case object Record extends Element {
    val defaultXOffset = 4f
    val defaultYOffset = 3f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape =
      new Ellipse2D.Float(scale * xoff, scale * yoff, scale * 16f, scale * 16f)
  }

  case object GoToBegin extends Element {
    val defaultXOffset = 4f
    val defaultYOffset = 3f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val end = GoToEnd.shape(scale, xoff = 0f, yoff = yoff)
      val at = AffineTransform.getScaleInstance(-1.0, 1.0)
      at.translate(-(end.getBounds2D.getWidth + xoff), 0)
      at.createTransformedShape(end)
    }
  }

  case object GoToEnd extends Element {
    val defaultXOffset = 4f
    val defaultYOffset = 3f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val play = Play.shape(scale * 0.7f, xoff = xoff, yoff = yoff)
      val res = new Area(play)
      val ba = new Rectangle2D.Float(scale * 11.5f + xoff, yoff, scale * 3f, scale * 14f)
      res.add(new Area(ba))
      res
    }
  }

  case object FastForward extends Element {
    val defaultXOffset = 2f
    val defaultYOffset = 3f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val play = Play.shape(scale * 0.7f, xoff = xoff, yoff = yoff)
      val p2 = AffineTransform.getTranslateInstance(scale * 10f, 0).createTransformedShape(play)
      val res = new Area(play)
      res.add(new Area(p2))
      res
    }
  }

  case object Rewind extends Element {
    val defaultXOffset = 1.5f
    val defaultYOffset = 3f

    def shape(scale: Float, xoff: Float, yoff: Float): Shape = {
      val ffwd = FastForward.shape(scale, xoff = 0, yoff = yoff)
      val at = AffineTransform.getScaleInstance(-1.0, 1.0)
      at.translate(-ffwd.getBounds2D.getWidth - xoff, 0)
      at.createTransformedShape(ffwd)
    }
  }

  case object Loop extends Element {
    // private val doRotate = true

    val defaultXOffset = 2f
    val defaultYOffset = 4f // if (doRotate) 3f else 1.5f

    def shape(scale: Float = 1f, xoff: Float = defaultXOffset, yoff: Float = defaultYOffset): Shape = {

      val res = new Area(new RoundRectangle2D.Float(0f, scale * 4f, scale * 22f, scale * 14f, scale * 10f, scale * 10f))
      res.subtract(new Area(new RoundRectangle2D.Float(0f + scale * 3f, scale * 7f, scale * 16f, scale * 8f, scale * 8f, scale * 8f)))

      val gp = new GeneralPath()
      gp.moveTo(0f, scale * 18f)
      gp.lineTo(scale * 11f, scale * 9f)
      gp.lineTo(scale * 11f, 0f)
      gp.lineTo(scale * 22f, 0f)
      gp.lineTo(scale * 22f, scale * 18f)

      gp.closePath()
      res.subtract(new Area(gp))
      val play = Play.shape(scale * 0.5f, /* xoff + */ 8f, /* yoff - 2.5f */ 0.5f)
      res.add(new Area(play))
      val rot = AffineTransform.getRotateInstance(math.Pi, scale * 11f, scale * 12f).createTransformedShape(res)
      res.add(new Area(rot))
      // val at = AffineTransform.getScaleInstance(1f, 0.8f)
      val at = AffineTransform.getScaleInstance(0.9f, 0.8f)
      // if (doRotate) {
        at.rotate(math.Pi * -0.2, scale * 11f, scale * 12f)
        at.preConcatenate(AffineTransform.getTranslateInstance(xoff, yoff - 3f))
      // } else {
      //  at.translate(xoff, yoff)
      // }
      at.createTransformedShape(res)
    }
  }

  private val segmentFirst  = "first"
  private val segmentMiddle = "middle"
  private val segmentLast   = "last"
  private val segmentOnly   = "only"

  trait ButtonStrip {
    def buttons: Seq[AbstractButtonType]
    def button(iconType: Element): Option[AbstractButtonType]
    def elements: Seq[Element]
    def element(button: AbstractButtonType): Option[Element]
  }

  protected trait ButtonStripImpl extends ButtonStrip {
    protected def actions: Seq[Action]

    protected def scheme: ColorScheme

    protected def makeButton(pos: String, action: Action): AbstractButtonType

    protected def addButtons(seq: Vec[AbstractButtonType]): Unit

    private val (buttonSeq, buttonMap, elementMap) = {
      var bMap = Map.empty[Element, AbstractButtonType]
      var tMap = Map.empty[AbstractButtonType, Action]
      var sq = Vec.empty[AbstractButtonType]
      val it = actions.iterator
      if (it.hasNext) {
        val n1 = it.next()
        val pos1 = if (it.hasNext) segmentFirst else segmentOnly
        val b1 = makeButton(pos1, n1)
        bMap += n1.element -> b1
        tMap += b1 -> n1
        sq :+= b1
        while (it.hasNext) {
          val n = it.next()
          val pos = if (it.hasNext) segmentMiddle else segmentLast
          val b = makeButton(pos, n)
          bMap += n.element -> b
          tMap += b -> n
          sq :+= b
        }
      }
      (sq, bMap, tMap)
    }

    addButtons(buttonSeq)

    final def buttons: Seq[AbstractButtonType] = buttonSeq
    final def button(element: Element): Option[AbstractButtonType] = buttonMap.get(element)
    final def elements: Seq[Element] = actions.map(_.element)
    final def element(button: AbstractButtonType): Option[Element] = elementMap.get(button).map(_.element)

    override def toString = "ButtonStrip"
  }
}

object Transport extends TransportCompanion {
  type AbstractButtonType = AbstractButton
  type ComponentType = JComponent
  type Action = javax.swing.Action with ActionLike

  protected def makeAction(icons: (Icon, Icon, Icon, Icon), element: Element, scale: Float, fun: => Unit): Action =
    new ActionImpl(icons, element, scale, fun)

  private final class JButtonStripImpl( protected val actions: Seq[ Action ], protected val scheme: ColorScheme )
    extends Box(BoxLayout.X_AXIS) with ButtonStripImpl {
    protected def addButtons(seq: Vec[AbstractButton]): Unit = seq.foreach(add)

    protected def makeButton(pos: String, action: Action): AbstractButton = {
      val b = new JButton(action)
      val (_, iconSelected, iconPressed, iconDisabled) = action.icons
      b.setSelectedIcon(iconSelected)
      b.setPressedIcon (iconPressed )
      b.setDisabledIcon(iconDisabled)
      b.setFocusable(false)
      b.putClientProperty("styleId", "icon-space")
      b.putClientProperty("JButton.buttonType", "segmentedCapsule") // "segmented" "segmentedRoundRect" "segmentedCapsule" "segmentedTextured" "segmentedGradient"
      b.putClientProperty("JButton.segmentPosition", pos)
      //         b.setMinimumSize( new Dimension( 10, 50 ))
      //         b.setPreferredSize( new Dimension( 50, 50 ))
      b
    }
  }

  def makeButtonStrip(actions: Seq[ActionElement], scale: Float, scheme: ColorScheme): JComponent with ButtonStrip = {
    val a = actions.map(_.apply(scale, scheme))
    new JButtonStripImpl(a, scheme)
  }

  private final class ActionImpl(val icons: (Icon, Icon, Icon, Icon), val element: Element, val scale: Float, fun: => Unit)
    extends AbstractAction(null, icons._1) with ActionLike {

    def icon   : Icon     = icons._1

    def actionPerformed(e: ActionEvent): Unit = fun
  }
}
