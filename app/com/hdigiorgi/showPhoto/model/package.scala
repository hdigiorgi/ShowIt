package com.hdigiorgi.showPhoto.model

import java.time.Instant
import cats.Later
import cats.syntax.option._

/**
  * LANGUAGE
  */

case class Language(code: String, name: String)
object Language {
  implicit def toLanguage(code: String): Option[Language] = code match {
    case English.code => English.some
    case Spanish.code => Spanish.some
    case German.code => German.some
    case Portuguese.code => Portuguese.some
    case Russian.code => Russian.some
    case French.code => French.some
    case _ => None
  }
  val default: Language = English
  val list: List[Language] =
    List(English, Spanish, Portuguese, German, French, Russian)

  object English extends Language("en", "english")
  object Spanish extends Language("es", "español")
  object Portuguese extends Language("pt", "português ")
  object German extends Language("de", "deutsch")
  object French extends Language("fr", "le français")
  object Russian extends Language("ru", "ру́сский язы́к")
}

/**
  * PRICE
  */
trait Price {
  def value(): Float
}
object Price {
  def apply(v: Float): Price = {
    if(v > 0) Priced(v) else Free
  }
}
object Free extends Price {
  override def value(): Float = 0
}
case class Priced(private val _value: Float) extends Price {
  override def value(): Float = _value
}

/**
  * LICENCE
  */
case class License(id: License.Id, price: Price, enabled: Boolean)
object License {
  sealed case class Id(id: String,
                       defaultPrice: Price,
                       enabledByDefault: Boolean)
  object Id {
    def apply(id: String): Id = ids.find(_.id == id).get
  }
  object LVL1 extends Id("L1", Priced(2), true)
  object LVL2 extends Id("L2", Priced(10), true)
  object LVL3 extends Id("L3", Priced(60), true)
  val ids = List(LVL1, LVL2, LVL3)
  def getDefaultLicenses(): List[License] = {
    ids.map( id => License(id, id.defaultPrice, id.enabledByDefault))
  }
}

/**
  * Item
  */
class Item(id: String,
           slug: String,
           private val _descriptionRaw: Later[String],
           private val _renderedDescription: Later[String],
           private val _title: Later[String],
           private val _summary: Later[String],
           imageCount: Int,
           createdAt: Instant) {
  def descriptionRaw() = _descriptionRaw.value
  def description() = _renderedDescription.value
  def title() = _title.value
  def summary = _summary.value
}

/**
  * Site
  */
case class Site(name: String, description: String, language: Language)

/**
  * Purchase
  */
object Purchase {
  abstract case class Status(name: String)
  object Started extends Status("STARTED")
  object Cancelled extends Status("CANCELLED")
  object Received extends Status("RECEIVED")
}

case class Purchase(purchaseId: String,
                    itemId: String,
                    status: Purchase.Status,
                    email: String,
                    price: Float,
                    startedAt: Instant,
                    endedAt: Option[Instant])

/**
  * File
  */
case class FileMeta(location: String,
                    itemId: String,
                    semantic: FileMeta.Semantic)
object FileMeta {
  abstract case class Semantic(id: String)
  object Thumbnail extends Semantic("THUMBNAIL")
  object DisplayImage extends Semantic("DISPLAY_IMAGE")
  object DownloadFile extends Semantic("FILE_TO_DOWNLOAD")
}

/**
  * Persistence
  */
trait PersistentInterface[A, B]{
  def update(element: A): Unit
  def read(key: B): Option[A]
  def delete(key: B): Unit
  def init(): Unit
}
trait LicensePI extends PersistentInterface[License, License.Id]
trait ItemPI extends PersistentInterface[Item, String]
trait SitePI extends PersistentInterface[Site, String]
trait PurchasePI extends PersistentInterface[Purchase, String]
trait FileMetaPI extends PersistentInterface[FileMeta, String]



/*
case class DB(license: PersistentLicense,
              item: PersistentItem,
              site: PersistentSite,
              purchase: PersistentPurchase,
              fileMeta: PersistentFileMeta)*/