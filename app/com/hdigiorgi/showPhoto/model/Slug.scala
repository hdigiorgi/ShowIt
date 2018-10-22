package com.hdigiorgi.showPhoto.model

import com.github.slugify.Slugify
import org.apache.commons.io.FilenameUtils

import scala.collection.JavaConverters._


case class Slug private(value: String) {
  def isEmpty: Boolean = Slug.empty.value.equals(this.value)
  override def toString: String = this.value
}

case class FileSlug private(value: String) {
  override def toString: String = this.value
  def withExtension(ext: String): FileSlug = {
    FileSlug(FilenameUtils.getBaseName(value) + "." + ext)
  }
  def withPrefix(pre: String): FileSlug = {
    val preSlug = Slug.fromString(pre)
    FileSlug(preSlug.value + "_" + this.value)
  }
  def baseName: String = {
    FilenameUtils.getBaseName(value)
  }
  def extension : String = FilenameUtils.getExtension(value)
}
object FileSlug {
  def apply(value: String): FileSlug = {
    val dotSeparator = "separator1dotseparator2dotseparator3"
    if(value.contains(dotSeparator)) throw new RuntimeException(s"invalid input $dotSeparator")
    val normalSlug = Slug.fromString(value, Map("." -> dotSeparator)).value
    val slugWithDot = normalSlug.replaceAll(f"($dotSeparator)+", ".")
    new FileSlug(slugWithDot)
  }

  def noSlugify(value: String): FileSlug = new FileSlug(value)

}

object Slug {

  def apply(value: String): Slug = {
    fromString(value)
  }

  def noSlugify(value: String): Slug = new Slug(value)

  def fromString(s: String, customReplacements: Map[String, String] = Map()) = {
    val replacements = customReplacements + ("-" -> "_")
    val slugficator = new Slugify()
      .withUnderscoreSeparator(true)
      .withLowerCase(true)
      .withTransliterator(true)
      .withCustomReplacements(replacements.asJava)
    val slugfied = slugficator.slugify(s)
    new Slug(slugfied)
  }

  val empty = Slug("")

}