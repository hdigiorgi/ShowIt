package com.hdigiorgi.showPhoto.model.db.sqlite.site

import cats.Later
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import com.hdigiorgi.showPhoto.model.post.SafeHtml
import com.hdigiorgi.showPhoto.model.site.{Site, SiteLink}
import com.hdigiorgi.showPhoto.model.SitePI
import slick.jdbc.SQLiteProfile.api._

object SQLSiteType {
  val TABLE_NAME = "SITE"
  type Name = String; val NAME_COLUMN = "NAME"
  type RawContent = String; val DESCRIPTION_RAW_COLUMN = "CONTENT_RAW"
  type RenderedContent = String; val DESCRIPTION_RENDERED_COLUMN = "CONTENT_RENDERED"
  type Links = String; val LINKS_COLUMN = "LINKS"

  type Tuple = (Name, RawContent, RenderedContent, Links)
  type SmallTuple = (Name, RenderedContent, Links)
}

class SQLSite(tag: Tag) extends Table[SQLSiteType.Tuple](tag, SQLSiteType.TABLE_NAME) {
  def name = column[SQLSiteType.Name](SQLSiteType.NAME_COLUMN)
  def descriptionRaw = column[SQLSiteType.RawContent](SQLSiteType.DESCRIPTION_RAW_COLUMN)
  def descriptionRendered = column[SQLSiteType.RenderedContent](SQLSiteType.DESCRIPTION_RENDERED_COLUMN)
  def links = column[SQLSiteType.Links](SQLSiteType.LINKS_COLUMN)
  override def * = (name, descriptionRaw, descriptionRendered, links)
  def smallTuple = (name, descriptionRendered, links)
}

class SQLSitePI() extends SitePI {
  private val table = TableQuery[SQLSite]

  override def update(site: Site): Unit = DB.runSyncThrowIfNothingAffected(
    table.update(toTuple(site))
  )

  override def read(): Site = _read().get

  def init(): SQLSitePI = {
    DB.ensureTableExists(table)
    ensureSiteExists()
    this
  }

  private def _read(): Option[Site] = {
    val read = table.map(_.smallTuple).take(1).result
    DB.runSync(read).headOption.map(fromTuple)
  }

  private def _insert(site: Site): Unit = DB.runSyncThrowIfNothingAffected{
    table += toTuple(site)
  }

  private def toTuple(site: Site): SQLSiteType.Tuple = {
    (site.name,
      site.rawContent,
      site.renderedContent.value,
      SiteLink.toString(site.links))
  }

  private def readRawDescription(): String = {
    val dbr = DB.runSync(table.map(_.descriptionRaw).take(1).result)
    dbr.headOption.getOrElse("")
  }

  private def fromTuple(tuple: SQLSiteType.SmallTuple): Site = {
    fromTuple((tuple._1, null, tuple._2, tuple._3))
  }
  private def fromTuple(tuple: SQLSiteType.Tuple): Site = {
    val rawDescription: String = tuple._2 match {
      case null => readRawDescription()
      case x => x
    }
    Site(name = tuple._1,
      rawDescription = Later(rawDescription),
      renderedDescription = SafeHtml.fromAlreadySafeHtml(tuple._3),
      links = SiteLink.fromString(tuple._4))
  }

  private def ensureSiteExists(): Unit = {
    if(_read().isEmpty){
      _insert(Site())
    }
  }

}


