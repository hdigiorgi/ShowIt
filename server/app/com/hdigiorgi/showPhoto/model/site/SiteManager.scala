package com.hdigiorgi.showPhoto.model.site

import com.hdigiorgi.showPhoto.model.{DBInterface, ErrorMessage, FatalErrorMsg, SitePI}
import play.api.Configuration
import scala.util.{Failure, Success, Try}

class SiteManager(private val db: SitePI) {
  private var _site: Option[Site] = None

  def site: Site = _site match {
    case Some(site) => site
    case None =>
      _site = Some(db.read())
      _site.get
  }

  def updateName(name: String): Either[ErrorMessage, Site] = {
    update(site.withName(name))
  }

  def updateDescription(description: String): Either[ErrorMessage, Site] = {
    update(site.withRawDescription(description))
  }

  def updateLinks(links: Seq[String]): Either[ErrorMessage, Site] = {
    update(site.withStingLinks(links))
  }

  private def update(site: Site): Either[ErrorMessage, Site] = {
    Try(db.update(site)) match {
      case Failure(t) => Left(FatalErrorMsg(t))
      case Success(_) =>
        _site = Some(site)
        Right(site)
    }
  }
}

object SiteManager {
  private var managers: Map[String, SiteManager] = Map.empty
  def apply()(implicit cfg: Configuration): SiteManager = {
    val env = cfg.get[String]("ENV")
    managers.get(env) match {
      case Some(mgr) => mgr
      case None =>
        val mgr = new SiteManager(DBInterface.getDB().site)
        this.managers = managers + (env -> mgr)
        mgr
    }
  }
}
