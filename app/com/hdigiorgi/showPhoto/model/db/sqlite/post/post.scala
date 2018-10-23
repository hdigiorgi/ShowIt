package com.hdigiorgi.showPhoto.model.db.sqlite.post

import java.time.Instant

import cats.Later
import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import com.hdigiorgi.showPhoto.model.post.{Post, PublicationStatus, SafeHtml, Title}
import slick.jdbc.SQLiteProfile.api._

object SQLitePostType {
  val TableName = "POST"
  type Id = String; val IdColumnName = "ID"
  type Title = String; val TitleColumnName = "TITLE"
  type Slug = String; val SlugColumnName = "SLUG"
  type CreationTime = Long; val CreationTimeColumnName = "CREATION_TIME"
  type RawContent = String; val RawContentColumnName = "RAW_CONTENT"
  type RenderedContent = String; val RenderedContentColumnName = "RENDERED_CONTENT"
  type PublicationStatus = String; val PublicationStatusColumnName = "PUBLICATION_STATUS"

  type TupleWithoutId = (Title, Slug, CreationTime, RawContent, RenderedContent, PublicationStatus)
  type Tuple = (Id, Title, Slug, CreationTime, RawContent, RenderedContent, PublicationStatus)
  type MiniTuple = (Id, Title, Slug, CreationTime, RenderedContent, PublicationStatus)
}

class SQLitePost(tag: Tag) extends Table[SQLitePostType.Tuple](tag, SQLitePostType.TableName) {
  def id= column[SQLitePostType.Id](SQLitePostType.IdColumnName, O.PrimaryKey)
  def title = column[SQLitePostType.Title](SQLitePostType.TitleColumnName)
  def slug = column[SQLitePostType.Slug](SQLitePostType.SlugColumnName, O.Unique)
  def creationTime = column[SQLitePostType.CreationTime](SQLitePostType.CreationTimeColumnName)
  def rawContent= column[SQLitePostType.RawContent](SQLitePostType.RawContentColumnName)
  def renderedContent = column[SQLitePostType.RenderedContent](SQLitePostType.RenderedContentColumnName)
  def publicationStatus = column[SQLitePostType.PublicationStatus](SQLitePostType.PublicationStatusColumnName)


  override def * = (id, title, slug, creationTime, rawContent, renderedContent, publicationStatus)
}

class SQLitePostPI() extends PostPI {
  private val table = TableQuery[SQLitePost]

  override def insert(element: Post): Unit = {
    val insert = table += toTuple(element)
    DB.runSyncThrowIfNothingAffected(insert)
  }

  override def update(element: Post): Unit = {
    val q = for {
      post <- table if post.id === element.id.value
    } yield post
    val update = q.update(toTuple(element))
    DB.runSyncThrowIfNothingAffected(update)
  }

  override def read(key: StringId): Option[Post] = read(_.id === key.value)

  override def readBySlug(slug: Slug): Option[Post] = read(_.slug === slug.value)

  override def delete(key: StringId): Unit = {
    val d = table.filter(_.id === key.value).delete
    DB.runSyncThrowIfNothingAffected(d)
  }

  override def readPaginated(page: Page): Seq[Post] = {
    val q = table.sortBy{ p => page.order match {
      case Ascending => p.creationTime.asc
      case Descending => p.creationTime.desc
    }}.drop(page.drop).take(page.take).result
    DB.runSync(q).map(fromTuple)
  }

  private def read[A](f: SQLitePost => Rep[Boolean]): Option[Post] = {
    val q = table.filter(f).map(p => {
      (p.id, p.title, p.slug, p.creationTime, p.renderedContent, p.publicationStatus)
    }).result
    DB.runSync(q).headOption.map(fromTuple)
  }

  private def readRawContent(key: String): Option[String] = {
    val q = table.filter(_.id === key).map(_.rawContent).result
    DB.runSync(q).headOption
  }

  private def toTupleWithoutId(post: Post): SQLitePostType.TupleWithoutId = {
    toTuple(post) match {
      case (_, title, slug, creationTime, rawContent, renderedContent, publicationStatus) =>
        (title, slug, creationTime, rawContent, renderedContent, publicationStatus)
    }
  }

  private def toTuple(post: Post): SQLitePostType.Tuple = {
    (post.id.value, post.title.value, post.slug.value, post.creationTime.getEpochSecond,
      post.rawContent, post.renderedContent.value, post.publicationStatus.name)
  }

  private def fromTuple(tuple: SQLitePostType.MiniTuple): Post = tuple match {
    case(id, title, slug, creationTime, renderedContent, publicationStatus) =>
      fromTuple((id, title, slug, creationTime, null, renderedContent, publicationStatus))
  }

  private def fromTuple(tuple: SQLitePostType.Tuple): Post = tuple match {
    case(id, title, slug, creationTime, rawContent, renderedContent, publicationStatus) =>
      val laterRawContent = Option(rawContent) match {
        case Some(_) => Later(renderedContent)
        case None => Later(readRawContent(id).get)
      }
      Post(id = StringId(id), title = Title(title), slug = Slug(slug),
        creationTime = Instant.ofEpochSecond(creationTime), rawContent = laterRawContent,
        renderedContent = SafeHtml.fromAlreadySafeHtml(renderedContent),
        publicationStatus = PublicationStatus(publicationStatus))
  }

  def init(): SQLitePostPI = {
    DB.ensureTableExists(table)
    this
  }
}