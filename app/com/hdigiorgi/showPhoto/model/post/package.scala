package com.hdigiorgi.showPhoto.model.post

import com.hdigiorgi.showPhoto.model.{Slug, StringId}
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

case class Post(id: StringId,
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
  def empty(id: StringId = StringId.random): Post = {
    new Post(id = id, slug = Slug.empty, contentRendered = SafeHtml.empty, title= Title.empty,
      creationTime = Instant.now(), modificationTime = Instant.now(), status = PublicationStatus.Unpublished,
      contentRawLater = Later.apply(""))
  }
}
