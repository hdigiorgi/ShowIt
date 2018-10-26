package com.hdigiorgi.showPhoto.model

import java.time.Instant
import java.util.UUID

import com.hdigiorgi.showPhoto.model.post.{Post, PublicationStatus}
import org.jasypt.util.password.StrongPasswordEncryptor
import play.api.Configuration

final case class InvalidModelException(private val message: String = "")
  extends Exception(message)

class ErrorMessage(origin: Symbol, private val msgSuffixId: String) {
  def id = f"errorMessage.$origin.$msgSuffixId"
  def message()(implicit i18n: play.api.i18n.Messages): String = i18n(id)
}
case class PostErrorMsg(private val _id: String) extends ErrorMessage('post, _id)
case class ImageErrorMsg(private val _id: String) extends ErrorMessage('image, _id)
case class TitleErrorMsg(private val _id: String) extends ErrorMessage('title, _id)
case class PubStatusErrorMsg(private val _id: String) extends ErrorMessage('publicationStatus, _id)

/**
  * ID
  */
case class StringId(value: String)
object StringId {
  def random: StringId = StringId(UUID.randomUUID().toString)
  implicit def fromString(s: String): StringId = StringId(s)
  implicit def toString(s: StringId): String = s.value
}
case class IntId(value: Int)
object IntId { def random: IntId = IntId(new java.security.SecureRandom().nextInt()) }
case class Email(value: String)

/**
  * LANGUAGE
  */

case class Language(code: String, name: String)
object Language {
  val English = Language("en", "english")
  val Spanish = Language("es", "español")
  val Portuguese = Language("pt", "português ")
  val German = Language("de", "deutsch")
  val French = Language("fr", "le français")
  val Russian = Language("ru", "ру́сский язы́к")

  val Default: Language = English
  val languages: List[Language] = List(English, Spanish, Portuguese, German, French, Russian)

  def toLanguage(code: String): Option[Language] = languages.find( _.code.contains(code) )
  def getLanguage(code: String): Language = toLanguage(code).getOrElse(Default)
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

class Role private(_id: IntId) {
  def id: IntId = _id
  override def toString: String = id match {
    case x if x == Role.Admin.id => "Admin"
    case x if x == Role.Contributor.id => "Contributor"
    case x if x == Role.Registered.id => "Registered"
    case x if x == Role.Guest.id => "Guest"
  }
  def > (that: Role): Boolean = id.value <  that.id.value
  def >=(that: Role): Boolean = id.value <= that.id.value
  def < (that: Role): Boolean = id.value >  that.id.value
  def <=(that: Role): Boolean = id.value >= that.id.value
}
object Role {
  def apply(value: IntId): Role = value match {
    case x if x == Admin.id => Admin
    case x if x == Contributor.id => Contributor
    case x if x == Registered.id => Registered
  }
  val Admin = new Role(IntId(0))
  val Contributor = new Role(IntId(1))
  val Registered = new Role(IntId(2))
  val Guest = new Role(IntId(69))
}

case class Password(value: String) {
  def is(plainPassword: String): Boolean = {
    val passwordEncryptor = new StrongPasswordEncryptor()
    passwordEncryptor.checkPassword(plainPassword, value)
  }
  override def toString: String = {
    val index = (value.length * 0.85).toInt
    f"Password(${value.substring(index)})"
  }
}
object Password {
  def apply(plainText: String): Password = {
    val passwordEncryptor = new StrongPasswordEncryptor()
    val encrypted = passwordEncryptor.encryptPassword(plainText)
    new Password(encrypted)
  }
  def fromEncrypted(encrypted: String): Password = {
    new Password(encrypted)
  }
}

case class User(id: StringId, email: Email, password: Password, role: Role)
object User {
  def defaultUsers: List[User] = List(
    User(StringId.random, Email("me@hdigiorgi.com"), Password("password"), Role.Admin)
  )
}

/**
  * Meta
  */
case class Meta(entry: String, value: String)

trait Order
object Ascending extends Order
object Descending extends Order

case class Page(number: Int, size: Int, order: Order = Descending) {
  def drop: Int = size * number
  def take: Int = size
}
object Page {
  val FirstDec: Page = Page(1,0, Descending)
  val First: Page = FirstDec
}

/**
  * Persistence
  */
trait PersistentInterface[A, B]{
  def insert(element: A): Unit
  def update(element: A): Unit
  def read(key: B): Option[A]
  def delete(key: B): Unit
}
trait LicensePI extends PersistentInterface[License, Grade]
trait SitePI extends PersistentInterface[Site, String]
trait PurchasePI extends PersistentInterface[Purchase, String]
trait FileMetaPI extends PersistentInterface[FileMeta, String]
trait MetaPI extends PersistentInterface[Meta, String]
trait UserPI extends PersistentInterface[User, StringId] {
  def readByEmail(email: String): Option[User]
}
trait PostPI extends PersistentInterface[Post, StringId] {
  def readPaginated(page: Page): Seq[Post]
  def readBySlug(slug: Slug): Option[Post]
}

trait DBInterface {
  def license: LicensePI
  def post: PostPI
  def site: SitePI
  def purchase: PurchasePI
  def meta: MetaPI
  def user: UserPI
  def init(configuration: Configuration): Unit
  def configuration: Configuration
  def destroy(): Unit

  protected def wrapDestroy[A](destroy: => A): A = {
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
  private def DB: DBInterface = db.sqlite.DB

  def wrap[A](op: DBInterface => A)(implicit configuration: Configuration): A = op(getDB())

  def apply()(implicit configuration: Configuration): DBInterface = getDB()

  def getDB()(implicit configuration: Configuration): DBInterface = {
    DB.init(configuration)
    this.DB
  }

  def post(implicit configuration: Configuration): PostPI = getDB.post
}