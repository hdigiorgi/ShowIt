package com.hdigiorgi.showPhoto.model

import java.io.{File, PrintWriter}

import com.hdigiorgi.showPhoto.model.files.{FullSize, MediumSize, SizeType, SmallSize}
import controllers.routes

import scala.io.Source
import scala.util.Try

class Color private(_r: Integer, _g: Integer, _b: Integer, _a: Integer) {
  def r: Integer = _r
  def g: Integer = _g
  def b: Integer = _b
  def a: Integer = _a
  def af: Float = a.toFloat / 255f
  def withAlpha(alpha: Integer): Color = Color(r,g,b, alpha)
  def rgb: String = f"rgb($r,$g,$b)"
  def rgba: String = f"rgba($r,$g,$b,$af)"
  def commaSeparated: String = f"$r,$g,$b"
  def isBright: Boolean = {
    val threshold = 120
    r > threshold || g > threshold || g > threshold
  }
  def isDark: Boolean = !isBright
  def brigthness: Integer = Seq(r,g,b).foldLeft(0)(Math.max(_,_))
}
object Color {
  def apply(r: Integer, g: Integer, b: Integer, a: Integer = 255): Color = {
    new Color(ensureRange(r), ensureRange(g), ensureRange(b), ensureRange(a))
  }

  private def ensureRange(v: Integer): Integer = {
    Math.min(Math.max(0, v), 255)
  }

  def fromCommaSeparated(input: String): Color = {
    val values = input.split(",").map(_.trim.toInt)
    Color(values(0), values(1), values(2))
  }

  def fromCommaSeparatedOpt(input: String): Option[Color] = Try(fromCommaSeparated(input)).toOption

  def increaseContrast(a: Color, b: Color, margin: Integer): (Color, Color) = {
    val brightnessDiff = Math.abs(a.brigthness-b.brigthness)
    val contrast = Math.max(brightnessDiff - margin, margin)
    (
      Color(a.r - contrast, a.g - contrast, a.b - contrast),
      Color(a.r + contrast, a.g + contrast, a.b + contrast)
    )
  }

  object BrightnessOrdering extends Ordering[Color] {
    override def compare(x: Color, y: Color): Int = {
      x.brigthness compareTo y.brigthness
    }
  }

  val BLACK = Color(0,0,0)
  val WHITE = Color(255,255,255)
}

case class Palette(private val _inColors: List[Color]) {

  lazy val colors: List[Color] = _inColors.sorted(Color.BrightnessOrdering)

  def saveToFile(destination: File): Unit = {
    val paletteString = colors.map(_.commaSeparated)
    destination.getParentFile.mkdirs()
    val pw = new PrintWriter(destination)
    paletteString foreach { color =>
      pw.println(color)
    }
    pw.close()
  }

  def calculate(): (Color, Color) = {
    val (dark, bright) = (colors.head, colors.last)
    val (darkest, brightest) = Color.increaseContrast(bright, dark,50)
    if(brightest.isBright && darkest.isBright) {
      (Color.BLACK, darkest)
    } else if (brightest.isDark && darkest.isDark) {
      (Color.WHITE, darkest)
    } else {
      (brightest, darkest)
    }
  }

  lazy val (vibrant, background) = calculate()

}
object Palette {
  def readFromFile(origin: File): Palette = {
    val source = Source.fromFile(origin)
    val colors = source.getLines().map(Color.fromCommaSeparatedOpt).filter(_.isDefined).map(_.get).toList
    source.close()
    Palette(colors)
  }
}

case class Image(elementId: String,
                 fileSlug: FileSlug,
                 palette: Palette) {
  def url(sizeType: SizeType): String = routes.PostController.image(elementId, sizeType.name, fileSlug.value).url
  def smallSizeUrl: String = url(SmallSize)
  def mediumSizeUrl: String = url(MediumSize)
  def fullSizeUrl: String = url(FullSize)
  def id: String = fileSlug.value
}
