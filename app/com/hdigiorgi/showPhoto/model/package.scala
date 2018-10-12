package com.hdigiorgi.showPhoto.model

import java.time.Instant

import cats.Later
import cats.syntax.option._
import org.jasypt.util.password.StrongPasswordEncryptor
import play.api.Configuration

final case class InvalidModelException(private val message: String = "")
  extends Exception(message)

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
  def value: Float
  def op(f: Float => Float): Price = Price(f(value))
}
object Price {
  def apply(v: Float): Price = {
    if(v > 0) Priced(v) else Free
  }
}
object Free extends Price {
  override def value: Float = 0
}
case class Priced(private val _value: Float) extends Price {
  override def value: Float = _value
}

/**
  * ENABLED
  */
trait Toggle {
  def boolean: Boolean
  def toggle: Toggle
}
object Toggle {
  def apply(x: Boolean): Toggle = x match {
    case true => Enabled
    case false => Disabled
  }
}
object Enabled extends Toggle {
  override def boolean = true
  override def toggle: Toggle = Disabled
}
object Disabled extends Toggle {
  override def boolean = false
  override def toggle: Toggle = Enabled
}

/**
  * GRADE
  */

case class Grade(n: Int) {
  if(n <= 0) throw InvalidModelException(f"grade should be > 0 (current $n)")
  def string: String = f"G$n"
  def int: Int = n
}

/**
  * LICENCE
  */
case class License(grade: Grade, price: Price, enabled: Toggle)
object License {
  val allowedGrades = List(Grade(1), Grade(2), Grade(3))
  val defaultLicenses: List[License] = List(
    License(Grade(1), Price(2), Enabled),
    License(Grade(2), Price(10), Enabled),
    License(Grade(3), Price(30), Enabled)
  )
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
  * User
  */

case class UserRole private(id: Int) {
  object Admin extends UserRole(0)
  object Contributor extends UserRole(1)
  object Registered extends UserRole(2)
  object Guest extends UserRole(69)
}

case class Password(private var _value: String) {
  _value = {
    val passwordEncryptor = new StrongPasswordEncryptor()
    passwordEncryptor.encryptPassword(_value)
  }
  def is(plainPassword: String): Boolean = {
    val passwordEncryptor = new StrongPasswordEncryptor()
    passwordEncryptor.checkPassword(plainPassword, _value)
  }
}

case class User(id: String, password: Password, role: UserRole)

/**
  * Meta
  */
case class Meta(entry: String, value: String)

/**
  * Persistence
  */
trait PersistentInterface[A, B]{
  def update(element: A): Unit
  def read(key: B): Option[A]
  def delete(key: B): Unit
}
trait LicensePI extends PersistentInterface[License, Grade]
trait ItemPI extends PersistentInterface[Item, String]
trait SitePI extends PersistentInterface[Site, String]
trait PurchasePI extends PersistentInterface[Purchase, String]
trait FileMetaPI extends PersistentInterface[FileMeta, String]
trait MetaPI extends PersistentInterface[Meta, String]

trait DBInterface {
  def license: LicensePI
  def item: ItemPI
  def site: SitePI
  def purchase: PurchasePI
  def meta: MetaPI
  def init(configuration: Configuration): Unit
  def configuration: Configuration
  def destroy(): Unit
  protected def wrapDestroy(destroy: => Unit): Unit = {
    val conf = configuration match {
      case null => None
      case _ => configuration.getOptional[String]("unsecure.allow_db_destroy")
    }
    conf match {
      case Some("allow") =>
        destroy
      case _ =>
        throw new SecurityException("the database should NOT be deleted")
    }
  }
}
object DBInterface {
  def DB: DBInterface = db.sqlite.DB

  def wrapCleanDB[A](op: DBInterface => A)(implicit configuration: Configuration): A = {
    DB.init(configuration)
    val r = op(DB)
    DB.destroy()
    r
  }

  def wrap[A](op: DBInterface => A)(implicit configuration: Configuration): A = {
    DB.init(configuration)
    op(DB)
  }

}