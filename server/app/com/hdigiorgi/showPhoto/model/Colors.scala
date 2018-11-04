package com.hdigiorgi.showPhoto.model

import java.io.{File, PrintWriter}

import com.hdigiorgi.showPhoto.model.files._
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
  def -(x: Integer) = Color(r-x, g-x, b-x, this.a)
  def +(x: Integer) = Color(r+x, g+x, b+x, this.a)
  def lightup: Color = {
    val diff = 255 - brightest
    this + diff
  }
  def darken: Color = {
    this - darkest
  }
  lazy val brightest: Integer = Seq(r,g,b).foldLeft(0)(Math.max(_,_))
  lazy val darkest: Integer = Seq(r,g,b).foldLeft(0)(Math.min(_,_))
  lazy val vibrance: Integer = Seq(r,g,b).combinations(2).toSeq.map{ case List(a,b) =>
      Math.abs(a-b)
  }.max
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

  object BrightnessOrdering extends Ordering[Color] {
    override def compare(x: Color, y: Color): Int = x.brightest compareTo y.brightest
  }
  object VibranceOrdering extends Ordering[Color] {
    override def compare(x: Color, y: Color): Int = x.vibrance compareTo y.vibrance
  }

  val BLACK = Color(0,0,0)
  val WHITE = Color(255,255,255)
}

case class Palette(colors: List[Color]) {

  def saveToFile(destination: File): Unit = {
    val paletteString = colors.map(_.commaSeparated)
    destination.getParentFile.mkdirs()
    val pw = new PrintWriter(destination)
    paletteString foreach { color =>
      pw.println(color)
    }
    pw.close()
  }

  def calculateWhichIsOneIsForeground(lightColorCount: Integer, darkColorCount: Integer, vibrant: Color, dark: Color): (Color, Color) = {
    if(lightColorCount > darkColorCount) {
      (dark-50, vibrant+100)
    } else {
      (vibrant+50, dark-100)
    }
  }

  def calculate(): (Color, Color) = {
    val darkColorCount = colors.count(_.isDark)
    val lightColorCount = colors.length - darkColorCount
    val orderedByVibrance = colors.sorted(Color.VibranceOrdering)
    val orderedByBrightness = colors.sorted(Color.BrightnessOrdering)
    val vibrant = orderedByVibrance.last
    val dark = orderedByBrightness.head
    val foregroundAndBackground = calculateWhichIsOneIsForeground(lightColorCount, darkColorCount, vibrant, dark)
    if(lightColorCount > darkColorCount) {
      (dark.darken, vibrant.lightup)
    } else {
      (vibrant.lightup, dark.darken)
    }

  }

  lazy val (foreground, background) = calculate()

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
  def blurSizeUrl: String = url(BlurSize)
  def id: String = fileSlug.value
}
