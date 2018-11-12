package com.hdigiorgi.showPhoto.model

import java.net.URL
import java.time.Instant
import java.util.UUID

import cats.Later
import com.hdigiorgi.showPhoto.model.post.{Post, PublicationStatus, SafeHtml}
import com.hdigiorgi.showPhoto.model.purchase.Purchase
import com.hdigiorgi.showPhoto.model.site.Site
import org.apache.logging.log4j.Logger
import org.jasypt.util.password.StrongPasswordEncryptor
import play.api.Configuration

import scala.util.Try


final case class InvalidModelException(private val message: String = "")
  extends Exception(message)

class ErrorMessage(origin: Symbol, private val msgSuffixId: String, val throwable: Option[Throwable] = None) {
  def id = f"errorMessage.${origin.name}.$msgSuffixId"
  def message()(implicit i18n: play.api.i18n.Messages): String = i18n(id)
  def withThrowable(t: Throwable): ErrorMessage = {
    new ErrorMessage(origin, msgSuffixId, Some(t))
  }
  def log(message: String = null)(implicit logger: Logger): Unit = {
    val msgOpt = Option(message)
    this.throwable match {
      case None =>
        val msg = msgOpt.map(_ + " " + this.id).getOrElse(this.id)
        logger.info(msg)
      case Some(throwableValue) =>
        msgOpt match {
          case None => logger.error(throwableValue)
          case Some(msg) => logger.error(msg, throwableValue)
        }
    }
  }
}
case class PostErrorMsg(private val _id: String) extends ErrorMessage('post, _id)
case class ImageErrorMsg(private val _id: String) extends ErrorMessage('image, _id)
case class AttachmentErrorMsg(private val _id: String) extends ErrorMessage('attachment, _id)
case class TitleErrorMsg(private val _id: String) extends ErrorMessage('title, _id)
case class PubStatusErrorMsg(private val _id: String) extends ErrorMessage('publicationStatus, _id)
case class SiteLinkErrorMsg(private val _id: String) extends ErrorMessage('siteLink, _id)
case class EmailErrorMsg(private val _id: String) extends ErrorMessage('email, _id)
case class FatalErrorMsg(t: Throwable) extends ErrorMessage('fatal, t.getMessage, Some(t)) {
  override def id = f"errorMessage.fatal"
  override def message()(implicit i18n: play.api.i18n.Messages): String =
    f"\n${t.getMessage}\n${t.getStackTrace}"
}

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
  * User
  */
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

case class User(id: StringId, email: Email, password: Password)
object User {
  def defaultUsers: List[User] = List(
    User(StringId.random, Email("me@hdigiorgi.com"), Password("password"))
  )
}

/**
  * Meta
  */
case class Meta(entry: String, value: String)

trait Order
object Ascending extends Order
object Descending extends Order

case class Page(index: Int, size: Int, order: Order = Descending) {
  def drop: Int = size * index
  def take: Int = size
  def +(n: Int): Page = Page(index + n, size, order)
  def -(n: Int): Page = Page(index - n, size, order)
  def page: Int = index + 1
}
object Page {
  val FirstDec: Page = Page(1,0, Descending)
  val First: Page = FirstDec
}

case class PaginatedResult[A](elements: Seq[A], page: Page, totalElements: Long) {
  def map[B](f: A => B): PaginatedResult[B] = {
    PaginatedResult[B](elements.map(f), page, totalElements)
  }
  def isEmpty: Boolean = elements.isEmpty
  def nonEmpty: Boolean = elements.nonEmpty
  def first: A = elements.head
  def firstOption: Option[A] = elements.headOption
  def nextPage(n: Integer = 1): Option[Page] = {
    if((page.index + n) <= totalIndexes) Some(page + n) else None
  }
  def previousPage(n: Integer = 1): Option[Page] = {
    if((page.index - n) >= 0) Some(page - 1) else None
  }
  def totalPages: Long = (totalElements.toFloat / page.size.toFloat).ceil.toLong
  def totalIndexes: Long = totalPages -1
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

trait SitePI {
  def update(element: Site): Unit
  def read(): Site
}
trait PurchasePI {
  def readMatching(purchase: Purchase): Option[Purchase]
  def insert(purchase: Purchase): Unit
  def delete(itemId: String, trackingCode: String)
}
trait MetaPI extends PersistentInterface[Meta, String]
trait UserPI extends PersistentInterface[User, StringId] {
  def readByEmail(email: String): Option[User]
}
trait PostPI extends PersistentInterface[Post, StringId] {
  def readPaginated(page: Page, publicationStatus: Option[PublicationStatus] = None): PaginatedResult[Post]
  def readBySlug(slug: Slug): Option[Post]
}

trait DBInterface {
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