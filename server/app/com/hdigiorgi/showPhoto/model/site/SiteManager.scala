package com.hdigiorgi.showPhoto.model.site

import java.io.File

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.files.{FileSystemInterface, SiteImagesDB, SizeType, SmallSize}
import com.hdigiorgi.showPhoto.model.post.PostManager
import play.api.Configuration

import scala.util.{Failure, Success, Try}

class SiteManager(dbi: DBInterface, fsi: FileSystemInterface) {
  private val db: SitePI = dbi.site
  private val imageDb: SiteImagesDB = fsi.siteImage
  private var _site: Option[Site] = None

  def site: Site = _site match {
    case Some(site) => site
    case None => reReadSite()
  }

  def updateName(name: String): Either[ErrorMessage, Site] = {
    update(site.withName(name))
  }

  def updateContent(content: String): Either[ErrorMessage, Site] = {
    update(site.withRawContent(content))
  }

  def updateLinks(links: Seq[String]): Either[ErrorMessage, Site] = {
    for {
      siteWithLinks <- site.withStringLinks(links)
      r <- update(siteWithLinks)
    } yield r
  }

  def updatePaypalEmail(email: String): Either[ErrorMessage, Site] = for {
    siteWithEmail <- site.withPaypalEmail(email)
    r <- update(siteWithEmail)
  } yield  r

  def processImage(file: File, name: FileSlug): Either[ErrorMessage, Seq[Image]] = {
    imageDb.process(file, SiteManager.ID, name) match {
      case Failure(exception) => PostManager.ErrorMessages.ErrorProcessingImage(exception)
      case Success(value) =>
        reReadSite()
        Right(value)
    }
  }

  def deleteImage(name: FileSlug): Either[ErrorMessage, Unit] = {
    imageDb.deleteImage(SiteManager.ID, name) match {
      case false => PostManager.ErrorMessages.ImageNotFound
      case true =>
        reReadSite()
        Right(Unit)
    }
  }

  def getPreviewableImage(name: FileSlug): Option[File] = {
    imageDb.getImageFileWithSuggestedSize(SiteManager.ID, SmallSize, name).map(_._1)
  }

  def listStoredImages(): Seq[Image] = imageDb.getStoredImages(SiteManager.ID)

  private def update(site: Site): Either[ErrorMessage, Site] = {
    Try(db.update(site)) match {
      case Failure(t) => Left(FatalErrorMsg(t))
      case Success(_) =>
        _site = Some(site)
        Right(site)
    }
  }

  private def reReadSite() = {
    _site = Some(db.read().withImages(imageDb.getStoredImages(SiteManager.ID)))
    _site.get
  }

}

object SiteManager {
  private val ID = "SITE"
  private var managers: Map[String, SiteManager] = Map.empty
  def apply()(implicit cfg: Configuration): SiteManager = {
    val env = cfg.get[String]("ENV")
    managers.get(env) match {
      case Some(mgr) => mgr
      case None =>
        val mgr = new SiteManager(DBInterface.getDB(), FileSystemInterface.get)
        this.managers = managers + (env -> mgr)
        mgr
    }
  }
}
