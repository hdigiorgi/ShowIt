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
  def lightup(factor: Double): Color = {
    this + (255 * factor).toInt
  }
  def darken(factor: Double): Color = {
    this - (255 * factor).toInt
  }

  def relativeLuminance(other: Color): Double = {
    if(this.luminance > other.luminance){
      (this.luminance  + 0.05) / (other.luminance + 0.05)
    } else {
      (other.luminance + 0.05) / (this.luminance  + 0.05)
    }

  }
  lazy val luminance: Double = {
    val rg = if(r<=10.0) r/3294.0 else Math.pow(r/269.0 + 0.0513, 2.4)
    val gg = if(g<=10.0) g/3294.0 else Math.pow(g/269.0 + 0.0513, 2.4)
    val bg = if(b<=10.0) b/3294.0 else Math.pow(b/269.0 + 0.0513, 2.4)
    0.2126 * rg + 0.7152 * gg + 0.0722 * bg
  }
  lazy val brightness: Integer = Seq(r,g,b).foldLeft(0)(Math.max(_,_))
  lazy val darkness: Integer = Seq(r,g,b).foldLeft(0)(Math.min(_,_))
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

  def brightest(a: Color, b: Color): Color = {
    if(a.brightness > b.brightness) a else b
  }

  def fromCommaSeparatedOpt(input: String): Option[Color] = Try(fromCommaSeparated(input)).toOption

  object BrightnessOrdering extends Ordering[Color] {
    override def compare(x: Color, y: Color): Int = x.brightness compareTo y.brightness
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

  def increaseContrast(a: Color, b: Color, af: (Color, Double) => Color, bf: (Color, Double) => Color): (Color, Color) = {
    val rl = a.relativeLuminance(b)
    val threadshold = 4.5
    if (rl < threadshold) {
      val factor = (1 - (rl / threadshold))
      (af(a,factor), bf(b,factor) )
    } else {
      (a, b)
    }
  }

  def calculateWhichIsOneIsForeground(lightColorCount: Integer, darkColorCount: Integer, vibrant: Color, dark: Color, bright: Color): (Color, Color) = {

    if(lightColorCount > darkColorCount) {
      // BRIGHT IMAGE
      val rl = dark.relativeLuminance(vibrant)
      increaseContrast(dark, vibrant, _.darken(_), _.lightup(_))
    } else {
      // DARK IMAGE
      increaseContrast(vibrant, dark, _.lightup(_), _.darken(_))
    }
  }

  def calculate(): (Color, Color) = {
    val darkColorCount = colors.count(_.isDark)
    val lightColorCount = colors.length - darkColorCount
    val orderedByVibrance = colors.sorted(Color.VibranceOrdering)
    val orderedByBrightness = colors.sorted(Color.BrightnessOrdering)
    val vibrant = orderedByVibrance.last
    val dark = orderedByBrightness.head
    val bright = orderedByBrightness.last
    calculateWhichIsOneIsForeground(lightColorCount, darkColorCount, vibrant, dark, bright)
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
  def id: String = fileSlug.value
  lazy val postUrl = new PostImageLinks(this)
  lazy val siteUrl = new SiteImageLinks(this)
}

trait ImageLinks{
  def url(sizeType: SizeType): String
  def small: String = url(SmallSize)
  def medium: String = url(MediumSize)
  def full: String = url(FullSize)
  def blur: String = url(BlurSize)
}

class PostImageLinks(image: Image) extends ImageLinks{
  def url(sizeType: SizeType): String =
    routes.PostController.image(image.elementId, sizeType.name, image.fileSlug.value).url
}

class SiteImageLinks(image: Image) extends ImageLinks{
  def url(sizeType: SizeType): String =
    routes.AdminSiteController.imageLoad(image.fileSlug.value).url
}