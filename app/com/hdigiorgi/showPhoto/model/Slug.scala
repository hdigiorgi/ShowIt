package com.hdigiorgi.showPhoto.model

import com.github.slugify.Slugify
import scala.collection.JavaConverters._


case class Slug private(value: String) {
  def isEmpty: Boolean = Slug.empty.value.equals(this.value)
  override def toString: String = this.value
}

case class FileSlug private(value: String) {
  override def toString: String = this.value
}
object FileSlug {
  def apply(value: String): FileSlug = {
    val dotSeparator = "_separator1_dot_separator2_dot_separator3_"
    if(value.contains(dotSeparator)) throw new RuntimeException(s"invalid input $dotSeparator")
    val normalSlug = Slug.fromString(value, Map("." -> dotSeparator)).value
    val slugWithDot = normalSlug.replace(dotSeparator, ".")
    new FileSlug(slugWithDot)
  }
}

object Slug {

  def apply(value: String): Slug = {
    fromString(value)
  }

  def ignoreAndPutRaw(value: String): Unit = new Slug(value)

  def fromString(s: String, customReplacements: Map[String, String] = Map()) = {
    val slugficator = new Slugify()
      .withUnderscoreSeparator(true)
      .withLowerCase(true)
      .withTransliterator(true)
      .withCustomReplacements(customReplacements.asJava)
    val slugfied = slugficator.slugify(s)
    new Slug(slugfied)
  }

  val empty = Slug("")

}