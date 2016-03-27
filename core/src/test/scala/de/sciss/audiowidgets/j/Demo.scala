package de.sciss.audiowidgets
package j

import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import java.awt.{BorderLayout, EventQueue, GridLayout}
import java.text.NumberFormat
import java.util.Locale
import javax.swing._
import javax.swing.text.NumberFormatter

import de.sciss.submin.Submin

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.util.Try

object Demo extends App with Runnable {
  val isDark = args.contains("--dark")
  Submin.install(isDark)
  EventQueue.invokeLater(this)

  def run(): Unit = {
    val f = new JFrame("AudioWidgets")
    f.getRootPane.putClientProperty("apple.awt.brushMetalLook", java.lang.Boolean.TRUE)
    val cp = f.getContentPane
    val p = new JPanel(new BorderLayout())
    p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20))

    val m = new PeakMeter()
    m.numChannels = 1
    m.caption = true
    m.borderVisible = true

    import LCDColors._
    val lcdColors = Vec(
      (grayFg, defaultBg /* new Color(0, 0, 0, 0) */ ),
      (blueFg, blueBg),
      (grayFg, grayBg),
      (redFg, redBg),
      (blackFg, blackBg)
    )
    val lcdGrid = new JPanel(new GridLayout(lcdColors.size, 1, 0, 6))
    val lb1 = lcdColors.zipWithIndex.map({
      case ((fg, bg), idx) =>
        val lcd = new LCDPanel
        if (bg.getAlpha > 0) lcd.setBackground(bg)
        val lb = new JLabel(s"00:00:0$idx")
        if (idx != 1 && idx != 4) {
          lb.putClientProperty("styleId", "noshade")
        }
        if (idx == 0 || idx == 1) {
          lb.setFont(LCDFont())
        } else {
          lb.putClientProperty("JComponent.sizeVariant", "small")
        }
        lb.setForeground(fg)
        lcd.add(lb)
        lcdGrid.add(lcd)
        lb
    }).head
    p.add(m, BorderLayout.WEST)
    p.add(Box.createHorizontalStrut(20), BorderLayout.CENTER)
    val p2 = new JPanel(new BorderLayout())
    p2.add(lcdGrid, BorderLayout.NORTH)

    val unitMs = new ParamFormat[Int] {
      val unit = UnitView("milliseconds", "ms")

      val formatter = new NumberFormatter(NumberFormat.getIntegerInstance(Locale.US))

      def adjust(in: Int, inc: Int): Int = {
        val res = in + inc
        if (inc < 0 && res > in) Int.MinValue
        else if (inc > 0 && res < in) Int.MaxValue
        else res
      }

      def parse(s: String): Option[Int] = Try(s.toInt).toOption

      def format(value: Int): String = value.toString
    }

    p2.add(new ParamField(0, unitMs :: Nil), BorderLayout.SOUTH)

    //    p2.add(new JLabel {
    //      setText("00:00:00")
    //      setFont(LCDFont())
    //    }, BorderLayout.SOUTH)

    val axis = new Axis
    axis.format = AxisFormat.Time()
    axis.minimum = 0.0
    axis.maximum = 34.56

    lazy val trnspActions = Seq(
      Transport.GoToBegin, Transport.Play, Transport.Stop, Transport.GoToEnd, Transport.Loop).map {
      case l@Transport.Loop => l.apply {
        trnsp.button(l).foreach(b => b.setSelected(!b.isSelected))
      }
      case e => e.apply {}
    }
    lazy val trnsp: JComponent with Transport.ButtonStrip = Transport.makeButtonStrip(trnspActions)

    p.add(p2, BorderLayout.EAST)
    cp.add(p, BorderLayout.CENTER)
    cp.add(axis, BorderLayout.NORTH)
    cp.add(trnsp, BorderLayout.SOUTH)

    f.pack()
    f.setLocationRelativeTo(null)
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    val t = new Timer(30, new ActionListener {
      val rnd = new util.Random()
      var peak = 0.5f
      var rms = 0f

      def actionPerformed(e: ActionEvent): Unit = {
        peak = math.max(0f, math.min(1f, peak + math.pow(rnd.nextFloat() * 0.5, 2).toFloat * (if (rnd.nextBoolean()) 1 else -1)))
        rms = math.max(0f, math.min(peak, rms * 0.98f + (rnd.nextFloat() * 0.02f * (if (rnd.nextBoolean()) 1 else -1))))
        m.update(Vec(peak, rms))
      }
    })
    val t2 = new Timer(1000, new ActionListener {
      var cnt = 0

      def actionPerformed(e: ActionEvent): Unit = {
        cnt += 1
        val secs = cnt % 60
        val mins = (cnt / 60) % 60
        val hours = (cnt / 3600) % 100
        lb1.setText((hours + 100).toString.substring(1) + ":" +
          (mins + 100).toString.substring(1) + ":" +
          (secs + 100).toString.substring(1))
      }
    })
    f.addWindowListener(new WindowAdapter {
      override def windowOpened(e: WindowEvent): Unit = {
        t.start()
        t2.start()
      }

      override def windowClosing(e: WindowEvent): Unit = {
        t.stop()
        t2.stop()
      }
    })

    f.setVisible(true)
  }
}