package com.hdigiorgi.showPhoto.model.post

import com.hdigiorgi.showPhoto.model.StringId
import com.github.slugify.Slugify
import java.time.Instant
import cats.Later
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.apache.commons.text.StringEscapeUtils

case class PublicationStatus(status: String)
object PublicationStatus {
  val Published = PublicationStatus("PUBLISHED")
  val Unpublished = PublicationStatus("UNPUBLISHED")
}

case class Slug(value: String) { self =>
  def isEmpty: Boolean = Slug.empty == self
}
object Slug {
  def fromString(s: String): Slug = {
    val slugStr = new Slugify()
      .withUnderscoreSeparator(true)
      .withLowerCase(true)
      .slugify(s)
    Slug(slugStr)
  }

  val empty = Slug("")
}

case class SafeHtml(value: String)
object SafeHtml {

  def fromUnsafeHtml(unsafe: String): SafeHtml = {
    val safe = Jsoup.clean(unsafe, Whitelist.relaxed())
    val unscaptedSafe = StringEscapeUtils.unescapeXml(safe)
    SafeHtml(unscaptedSafe)
  }

  def fromUnsafeMarkdown(unsafeMarkdown: String): SafeHtml = {
    val parser = Parser.builder().build()
    val parsed = parser.parse(unsafeMarkdown)
    val renderer = HtmlRenderer.builder().build()
    val unsafeHtml = renderer.render(parsed)
    fromUnsafeHtml(unsafeHtml)
  }

  val empty = SafeHtml("")
}

case class Title(value: String)
object Title {
  val empty = Title("")
}

class Post(id: StringId,
           slug: Slug,
           contentRendered: SafeHtml,
           title: Title,
           creationTime: Instant,
           modificationTime: Instant,
           status: PublicationStatus,
           contentRawLater: Later[String]) {
  def contentRaw: String = contentRawLater.value
}

object Post {
  def empty(): Post = {
    new Post(id = StringId.random, slug = Slug.empty, contentRendered = SafeHtml.empty, title= Title.empty,
      creationTime = Instant.now(), modificationTime = Instant.now(), status = PublicationStatus.Unpublished,
      contentRawLater = Later.apply(""))
  }
}
