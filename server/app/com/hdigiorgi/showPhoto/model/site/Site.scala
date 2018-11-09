package com.hdigiorgi.showPhoto.model.site

import java.io.StringReader
import java.net.URL

import cats.Later
import com.hdigiorgi.showPhoto.model.{ErrorMessage, SiteLinkErrorMsg}
import com.hdigiorgi.showPhoto.model.post.SafeHtml
import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class SiteLink(raw: String) {
  import SiteLink._

  def valid: Either[ErrorMessage, SiteLink] = url match {
    case None => InvalidLink
    case Some(_) => Right(this)
  }

  def href: String = url match {
    case Some(u) => u.toString
    case None => raw
  }

  lazy val url: Option[URL] = {
    lazy val tryRaw  = Try(new URL(raw))
    lazy val tryHttp = Try(new URL(f"http://$raw"))
    tryRaw.orElse(tryHttp).toOption
  }

  lazy val faIcon: String = {
    getFaIcon(url.map(_.getHost).getOrElse(raw)).getOrElse("")
  }
}
object SiteLink {
  val InvalidLink = Left(SiteLinkErrorMsg("invalidLink"))

  def toString(links: Seq[SiteLink]): String = {
    val strs = links.map(_.raw)
    CSVFormat.DEFAULT.format(strs : _*)
  }

  def fromString(siteLinkString: String): Seq[SiteLink] = {
    val in = new StringReader(siteLinkString)
    val records = CSVFormat.DEFAULT.parse(in).iterator()
    if(!records.hasNext) return Seq.empty
    val iterator = records.next().iterator()
    val r = scala.collection.mutable.ListBuffer[SiteLink]()
    while(iterator.hasNext) {
      val link = iterator.next()
      r += SiteLink(link)
    }
    r
  }

  private val faLinksDef = Seq(
    Seq("fab fa-twitter", "twitter.com", "t.co"),
    Seq("fab fa-instagram", "instagram.com"),
    Seq("fab fa-youtube", "youtube.com", "youtu.be"),
    Seq("fab fa-google-plus", "plus.google.com"),
    Seq("fab fa-facebook", "facebook.com", "fb.me"),
    Seq("fab fa-vk", "plus.google.com"),
    Seq("fab fa-linkedin", "linkedin.com", "linked.in"),
    Seq("fas fa-external-link-alt", "")
  )

  private def getFaIcon(domain: String): Option[String] = {
    faLinksDef.find{ linkDef =>
      val toMatch = linkDef.tail
      toMatch.exists(domain.startsWith)
    }.map(_.head)
  }
}
class Site private (_inName: Option[String] = None,
                    _inRawDescription: Option[Later[String]] = None,
                    _inRenderedDescription: Option[SafeHtml] = None,
                    _inLinks: Seq[SiteLink] = Seq()) {
  private var _name = _inName.getOrElse("")
  def name: String = _name
  def withName(name: String): Site = {
    val site = new Site(this)
    site._name = name
    site
  }

  private var _rawDescription = _inRawDescription.getOrElse(Later(new String()))
  def rawDescription: String = _rawDescription.value
  def withRawDescription(descriptionRaw: Later[String]): Site = {
    val site = new Site(this)
    site._rawDescription = descriptionRaw
    site
  }
  def withRawDescription(rawDescription: String): Site = {
    val site = new Site(this)
    site._rawDescription = Later(rawDescription)
    site._renderedDescription = SafeHtml.fromUnsafeMarkdown(rawDescription)
    site
  }

  private var _renderedDescription: SafeHtml = _inRenderedDescription.getOrElse(SafeHtml.empty)
  def renderedDescription: SafeHtml = _renderedDescription

  private var _links = _inLinks
  def links: Seq[SiteLink] = _links
  def withStingLinks(links: Seq[String]): Either[ErrorMessage, Site] = {
    val siteLinks = links.map(SiteLink(_))
    siteLinks.find(_.valid.isLeft) match {
      case Some(invalidSiteLink) => Left(invalidSiteLink.valid.left.get)
      case None =>
        Right(withLinks(siteLinks))
    }
  }
  def withLinks(links: Seq[SiteLink]): Site = {
    val site = new Site(this)
    site._links = links
    site
  }

  override def toString: String = {
    f"Site($name,$rawDescription,$links)"
  }

  override def equals(thatObj: Any): Boolean = {
    if(!thatObj.isInstanceOf[Site]) return false
    val that = thatObj.asInstanceOf[Site]
    this.name.equals(that.name) &&
    this.links.toList.equals(that.links.toList) &&
    this.rawDescription.equals(that.rawDescription)
  }

  private def this(site: Site){
    this(_inName = Some(site.name),
      _inRawDescription = Some(Later(site.rawDescription)),
      _inRenderedDescription = Some(site.renderedDescription),
      _inLinks = site.links)
  }
}

object Site{
  def apply(): Site = new Site()
  def apply(name: String, description: String, links: Seq[String]): Site = {
    new Site(
      _inName = Some(name),
      _inRawDescription = Some(Later(description)),
      _inRenderedDescription = Some(SafeHtml.fromUnsafeMarkdown(description)),
      _inLinks = links.map(SiteLink(_))
    )
  }
  def apply(name: String, rawDescription: Later[String], renderedDescription: SafeHtml, links: Seq[SiteLink]): Site = {
    new Site(_inName = Some(name), _inRawDescription = Some(rawDescription), _inRenderedDescription = Some(renderedDescription),
      _inLinks = links)
  }
}
