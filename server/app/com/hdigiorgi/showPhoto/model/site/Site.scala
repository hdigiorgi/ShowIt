package com.hdigiorgi.showPhoto.model.site

import java.io.StringReader
import java.net.URL

import akka.http.scaladsl.model.headers.CacheDirectives.public
import cats.Later
import com.hdigiorgi.showPhoto.model.{ErrorMessage, Image, SiteLinkErrorMsg}
import com.hdigiorgi.showPhoto.model.post._
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
                    _inRawContent: Option[Later[String]] = None,
                    _inRenderedContent: Option[SafeHtml] = None,
                    _inLinks: Seq[SiteLink] = Seq(),
                    _inImages: Seq[Image] = Seq.empty)
    extends ImageHolder[Site] with MarkdownContentHolder[Site]{
  setMutableImageHolderImages(_inImages)
  setMutableMarkdownContent(_inRawContent, _inRenderedContent)

  def paypalEmail: Option[String] = Some("dd")

  private var _name = _inName.getOrElse("")
  def name: String = _name
  def withName(name: String): Site = {
    val site = new Site(this)
    site._name = name
    site
  }

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
    f"Site($name,$rawContent,$links)"
  }

  override def equals(thatObj: Any): Boolean = {
    if(!thatObj.isInstanceOf[Site]) return false
    val that = thatObj.asInstanceOf[Site]
    this.name.equals(that.name) &&
    this.links.toList.equals(that.links.toList) &&
    this.rawContent.equals(that.rawContent)
  }

  override def copyMe(): Site = new Site(this)
  private def this(site: Site){
    this(_inName = Some(site.name),
      _inRawContent = Some(site._possiblyNotEvaluatedRawContent),
      _inRenderedContent = site._possiblyNotEvaluatedRenderedContent,
      _inLinks = site.links)
  }


}

object Site{
  def apply(): Site = new Site()
  def apply(name: String, description: String, links: Seq[String]): Site = {
    new Site(
      _inName = Some(name),
      _inRawContent = Some(Later(description)),
      _inRenderedContent = Some(SafeHtml.fromUnsafeMarkdown(description)),
      _inLinks = links.map(SiteLink(_))
    )
  }
  def apply(name: String, rawDescription: Later[String], renderedDescription: SafeHtml, links: Seq[SiteLink]): Site = {
    new Site(_inName = Some(name), _inRawContent = Some(rawDescription), _inRenderedContent = Some(renderedDescription),
      _inLinks = links)
  }
}
