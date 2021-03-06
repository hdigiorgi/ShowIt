package com.hdigiorgi.showPhoto.model.db.sqlite.post

import java.time.Instant

import cats.Later
import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import com.hdigiorgi.showPhoto.model.post.{Post, PublicationStatus, SafeHtml, Title}
import slick.jdbc.SQLiteProfile.api._

object SQLPostType {
  val TableName = "POST"
  type Id = String; val IdColumnName = "ID"
  type Title = String; val TitleColumnName = "TITLE"
  type Slug = String; val SlugColumnName = "SLUG"
  type CreationTime = Long; val CreationTimeColumnName = "CREATION_TIME"
  type RawContent = String; val RawContentColumnName = "RAW_CONTENT"
  type RenderedContent = String; val RenderedContentColumnName = "RENDERED_CONTENT"
  type PublicationStatus = String; val PublicationStatusColumnName = "PUBLICATION_STATUS"
  type Price = Float; val PriceColumnName = "PRICE"

  type TupleWithoutId = (Title, Slug, CreationTime, RawContent, RenderedContent, PublicationStatus, Price)
  type Tuple = (Id, Title, Slug, CreationTime, RawContent, RenderedContent, PublicationStatus, Price)
  type MiniTuple = (Id, Title, Slug, CreationTime, RenderedContent, PublicationStatus, Price)
}

class SQLPost(tag: Tag) extends Table[SQLPostType.Tuple](tag, SQLPostType.TableName) {
  def id= column[SQLPostType.Id](SQLPostType.IdColumnName, O.PrimaryKey)
  def title = column[SQLPostType.Title](SQLPostType.TitleColumnName)
  def slug = column[SQLPostType.Slug](SQLPostType.SlugColumnName, O.Unique)
  def creationTime = column[SQLPostType.CreationTime](SQLPostType.CreationTimeColumnName)
  def rawContent= column[SQLPostType.RawContent](SQLPostType.RawContentColumnName)
  def renderedContent = column[SQLPostType.RenderedContent](SQLPostType.RenderedContentColumnName)
  def publicationStatus = column[SQLPostType.PublicationStatus](SQLPostType.PublicationStatusColumnName)
  def price = column[SQLPostType.Price](SQLPostType.PriceColumnName)

  override def * = (id, title, slug, creationTime, rawContent, renderedContent, publicationStatus, price)
}

class SQLPostPI() extends PostPI {
  private val table = TableQuery[SQLPost]

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

  override def readPaginated(page: Page, publicationStatus: Option[PublicationStatus]): PaginatedResult[Post] = {
    val baseQuery = table.sortBy{ p => page.order match {
      case Ascending => p.creationTime.asc
      case Descending => p.creationTime.desc
    }}
    val filteredQuery = publicationStatus.map{ status =>
      baseQuery.filter(_.publicationStatus === status.name)
    }.getOrElse(baseQuery)
    val paginatedQuery = filteredQuery.drop(page.drop).take(page.take).result
    val elements = DB.runSync(paginatedQuery).map(fromTuple)
    val count = DB.runSync(table.length.result)
    PaginatedResult(elements, page, count)
  }


  override def delete(key: StringId): Unit = {
    val d = table.filter(_.id === key.value).delete
    DB.runSyncThrowIfNothingAffected(d)
  }

  private def read[A](f: SQLPost => Rep[Boolean]): Option[Post] = {
    val q = table.filter(f).map(p => {
      (p.id, p.title, p.slug, p.creationTime, p.renderedContent, p.publicationStatus, p.price)
    }).result
    DB.runSync(q).headOption.map(fromTuple)
  }

  private def readRawContent(key: String): Option[String] = {
    val q = table.filter(_.id === key).map(_.rawContent).result
    DB.runSync(q).headOption
  }

  private def toTuple(post: Post): SQLPostType.Tuple = {
    val price: Float = post.price.map(_.baseValue).getOrElse(-1f)
    (post.id.value, post.title.value, post.slug.value, post.creationTime.toEpochMilli,
      post.rawContent, post.renderedContent.value, post.publicationStatus.name, price)
  }

  private def fromTuple(tuple: SQLPostType.MiniTuple): Post = {
    tuple match {
      case(id, title, slug, creationTime, renderedContent, publicationStatus, price) =>
        fromTuple((id, title, slug, creationTime, null, renderedContent, publicationStatus, price))
    }
  }

  private def fromTuple(tuple: SQLPostType.Tuple): Post = tuple match {
    case(id, title, slug, creationTime, rawContent, renderedContent, publicationStatus, price) =>
      val laterRawContent = Option(rawContent) match {
        case Some(_) => Later(renderedContent)
        case None => Later(readRawContent(id).get)
      }
      Post(id = StringId(id), title = Title(title), slug = Slug(slug),
        creationTime = Instant.ofEpochMilli(creationTime), rawContent = laterRawContent,
        renderedContent = SafeHtml.fromAlreadySafeHtml(renderedContent),
        publicationStatus = PublicationStatus(publicationStatus),
        price = price)
  }

  def init(): SQLPostPI = {
    DB.ensureTableExists(table)
    this
  }
}