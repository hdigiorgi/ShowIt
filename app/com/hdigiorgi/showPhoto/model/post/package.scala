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

case class SafeHtml private (value: String)
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

  def fromAlreadySafeHtml(safeHtml: String): SafeHtml = SafeHtml(safeHtml)

  val empty = SafeHtml("")
}

case class Title(value: String)
object Title {
  val empty = Title("")
}

class Post private (_inId: Option[StringId] = None,
                    _inTitle: Option[Title] = None,
                    _inSlug: Option[Slug] = None,
                    _inCreationTime: Option[Instant] = None,
                    _inPublicationStatus: Option[PublicationStatus] = None,
                    _inRawContent: Option[Later[String]] = None,
                    _inRenderedContent: Option[SafeHtml] = None) {
  val id: StringId = _inId.getOrElse(StringId.random)

  private var _title = _inTitle.getOrElse(Title(""))
  def title: Title = _title
  def setTitle(title: Title): Post = {
    _title = title
    this.setSlug(Slug(title.value))
  }

  private var _customSlug: Slug = _inSlug.getOrElse(Slug.empty)
  def slug: Slug = _customSlug
  def setSlug(slug: Slug): Post = {
    _customSlug = slug
    this
  }

  private var _rawContent = Later(new String())
  def rawContent: String = _rawContent.value
  def setRawContent(contentRaw: String): Unit = {
    _rawContent = Later(contentRaw)
    _renderedContent = SafeHtml.fromUnsafeMarkdown(_rawContent.value)
  }
  def setRawContent(contentRaw: Later[String]): Unit = {
    _rawContent = contentRaw
  }

  private var _creationTime = _inCreationTime.getOrElse(Instant.now())
  def creationTime: Instant = _creationTime
  def setCreationTime(ct: Instant): Post = {
    _creationTime = ct
    this
  }

  private var _publicationStatus = _inPublicationStatus.getOrElse(PublicationStatus.Unpublished)
  def publicationStatus: PublicationStatus = _publicationStatus
  def setPublicationStatus(status : PublicationStatus): Unit = _publicationStatus = status

  private var _renderedContent: SafeHtml = _inRenderedContent.getOrElse(SafeHtml.empty)
  def renderedContent: SafeHtml = _renderedContent

  override def toString: String = f"Post(${this.id.value},${this.title.value})"

  override def equals(that: Any): Boolean =
    that match {
      case that: Post => this.id.equals(that.id) &&
        this.title.equals(that.title) &&
        this.slug.equals(that.slug) &&
        this.creationTime.getEpochSecond == that.creationTime.getEpochSecond &&
        this.renderedContent.equals(that.renderedContent) &&
        this.publicationStatus.equals(that.publicationStatus)
      case _ => false
    }

  override def hashCode: Int =  this.id.value.hashCode
}

object Post {

  def apply(): Post = new Post(_inId = Some(StringId.random))
  def apply(id: StringId): Post = new Post(_inId = Some(id))

  def apply(id: StringId, title: Title, slug: Slug, creationTime: Instant, rawContent: Later[String],
            renderedContent: SafeHtml): Post = {
    new Post(_inId = Some(id), _inTitle = Some(title), _inSlug = Some(slug),
             _inCreationTime = Some(creationTime), _inRawContent = Some(rawContent))
  }
}
