package com.hdigiorgi.showPhoto.model

import java.io.{File, PrintWriter}

import com.hdigiorgi.showPhoto.model.files.{FullSize, MediumSize, SizeType, SmallSize}
import controllers.routes

import scala.io.Source
import scala.util.Try

case class Color(r: Integer, g: Integer, b: Integer, a: Integer = 255) {
  def rgb: String = f"rgb($r,$g,$b)"
  def rgba: String = f"rgba($r,$g,$b,$a)"
  def commaSeparated: String = f"$r,$g,$b"
}
object Color {
  def fromCommaSeparated(input: String): Color = {
    val values = input.split(",").map(_.trim.toInt)
    Color(values(0), values(1), values(2))
  }
  def fromCommaSeparatedOpt(input: String): Option[Color] = Try(fromCommaSeparated(input)).toOption

}

case class Palette(colors: Seq[Color]) {
  def saveToFile(destination: File): Unit = {
    val paletteString = colors.map(_.commaSeparated)
    destination.getParentFile.mkdirs()
    val pw = new PrintWriter(destination)
    paletteString foreach { color =>
      pw.println(color)
    }
    pw.close()
  }
}
object Palette {
  def readFromFile(origin: File): Palette = {
    val source = Source.fromFile(origin)
    val colors = source.getLines().map(Color.fromCommaSeparatedOpt).filter(_.isDefined).map(_.get).toSeq
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
