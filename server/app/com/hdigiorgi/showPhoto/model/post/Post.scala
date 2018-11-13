package com.hdigiorgi.showPhoto.model.post

import com.hdigiorgi.showPhoto.model._
import java.time.Instant

import cats.Later
import com.hdigiorgi.showPhoto.model.files.FileEntry
import org.apache.commons.lang3.math.NumberUtils
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.apache.commons.text.StringEscapeUtils

import scala.util.{Failure, Success, Try}

case class PublicationStatus(name: String){
  import PublicationStatus.ErrorMessages._

  def toggle: PublicationStatus = this match {
    case Published => Unpublished
    case _ => Published
  }
  def validateToggle(to: PublicationStatus): Either[ErrorMessage, PublicationStatus] = {
    if(this == to) return AlreadyInThatState
    Right(this)
  }
  def isPublished: Boolean = this == Published
  def isUnpublished: Boolean = this == Unpublished
}
object Published extends PublicationStatus("PUBLISHED")
object Unpublished extends PublicationStatus("UNPUBLISHED")
object PublicationStatus {
  object ErrorMessages {
    val AlreadyInThatState = Left(PubStatusErrorMsg("validations.status.alreadyInThatState"))
  }
}

case class SafeHtml private (value: String)
object SafeHtml {

  def fromUnsafeHtml(unsafe: String): SafeHtml = {
    val safe = Jsoup.clean(unsafe, Whitelist.relaxed())
    val unscaptedSafe = StringEscapeUtils.unescapeXml(safe)
    SafeHtml(unscaptedSafe)
  }

  def fromUnsafeMarkdown(unsafeMarkdown: String): SafeHtml = {
    val parser = Parser.builder().build()
    val parsed = parser.parse(unsafeMarkdown)
    val renderer = HtmlRenderer.builder().build()
    val unsafeHtml = renderer.render(parsed)
    fromUnsafeHtml(unsafeHtml)
  }

  def fromAlreadySafeHtml(safeHtml: String): SafeHtml = SafeHtml(safeHtml)

  val empty = SafeHtml("")
}

case class Title private (value: String) {
  import Title.ErrorMessages._

  def validate(postId: StringId, db: PostPI): Either[ErrorMessage, Title] = {
    for {
      _ <- validateLength()
      r <- validateExistingSlug(postId, db)
    } yield r
  }

  private def validateExistingSlug(postId: StringId, db: PostPI): Either[ErrorMessage, Title] = {
    val read = db.readBySlug(Slug(this.value))
    if (read.exists(_.id != postId)) LeadAlreadyExistingSlug else Right(this)
  }

  private def validateLength(): Either[ErrorMessage, Title] = {
    if(value.length <= 3) return ToShort
    if(value.length > 100) return ToLong
    if(Slug(value).value.length <= 3) return LeadToShortSlug
    Right(this)
  }
}

object Title {
  val empty = Title("")
  implicit def fromString(s: String): Title = Title(s.trim)

  object ErrorMessages {
    val ToShort = Left(TitleErrorMsg("validations.toShort"))
    val ToLong = Left(TitleErrorMsg("validations.toLong"))
    val LeadAlreadyExistingSlug = Left(TitleErrorMsg("validations.existingSlug"))
    val LeadToShortSlug = Left(TitleErrorMsg("validations.shortSlug"))
  }
}

case class Price(baseValue: Float, percentageFee: Float = 0.0f, fixedFee: Float = 0.0f) {
  if(percentageFee < 0.0f || percentageFee > 1.0) throw new IllegalStateException(f"invalid percentage fee $percentageFee")
  if(fixedFee < 0.0f) throw new IllegalStateException(f"fixed fee can't be negative")

  def totalPrice: Float = baseValue + fees

  def fees: Float = baseValue * percentageFee  +fixedFee

  def percentageFeeInt: Integer = (percentageFee * 100.0f).toInt

  def withPercentageFee(percentage: Float): Price = Price(this.baseValue, percentage, this.fixedFee)

  def withFixedFee(fixed: Float): Price = Price(this.baseValue, this.percentageFee, fixed)
    
}

object Price {
  import ErrorMessenges._

  def getOpt(input: Float): Option[Price] = {
    if(input <= 0) return None
    Some(Price(input))
  }

  def validated(input: String): Either[PriceErrorMsg, Price] = {
    if(input.contains(",")) return NoCommaAllowed
    Try(input.trim.toFloat) match {
      case Failure(_) => InvalidNumber
      case Success(number) => number match {
        case n if n < 0 => NonPositive
        case n if n < MIN_PRICE => ToLow
        case n if n > MAX_PRICE => ToMuch
        case _ => Right(Price(number))
      }
    }
  }

  object ErrorMessenges {
    val InvalidNumber = Left(PriceErrorMsg("invalid.number"))
    val NonPositive = Left(PriceErrorMsg("invalid.nonPositive"))
    val ToLow = Left(PriceErrorMsg("invalid.toLow"))
    val ToMuch = Left(PriceErrorMsg("invalid.toMuch"))
    val NoCommaAllowed = Left(PriceErrorMsg("invalid.noCommaAllowed"))
  }

  private val MAX_PRICE = 2000
  private val MIN_PRICE = 1
}

trait SelfCopyMutable[A <: SelfCopyMutable[A]] {
  protected def copyMe(): A
  protected def mutatingCopy(modify: A => Unit): A = {
    val `new` = copyMe()
    modify(`new`)
    `new`
  }
}

trait ImageHolder[A <: ImageHolder[A]] extends SelfCopyMutable[A] {
  protected var _images: Seq[Image] = Seq.empty

  def images: Seq[Image] = _images

  def withImages(images: Seq[Image]): A = {
    mutatingCopy{ c =>
      c._images = images
    }
  }

  def randomImage: Option[Image] = {
    val drop = Math.min(images.size -1, (Math.random()*images.size).ceil.toInt -1)
    images.drop(drop) match {
      case Seq() => None
      case Seq(image, _*) => Some(image)
    }
  }

  protected def setMutableImageHolderImages(images: Seq[Image] = Seq.empty) {
    _images = images
  }
}

trait AttachmentHolder[A <: AttachmentHolder[A]] extends SelfCopyMutable[A] {
  protected var _attachments: Seq[FileEntry] = Seq.empty
  def attachments: Seq[FileEntry] = _attachments
  def withAttachments(attachments: Seq[FileEntry]): A = mutatingCopy{
    _._attachments = attachments
  }
}

trait MarkdownContentHolder[A <: MarkdownContentHolder[A]] extends SelfCopyMutable[A] {
  private var _rawContent: Later[String] = _
  private var _renderedContent: Option[SafeHtml] = _

  def _possiblyNotEvaluatedRawContent: Later[String]= _rawContent
  def rawContent: String = _rawContent.value

  def _possiblyNotEvaluatedRenderedContent: Option[SafeHtml]= _renderedContent
  def renderedContent: SafeHtml = {
    _renderedContent match {
      case Some(alreadyRendered) => alreadyRendered
      case None =>
        val rendered = SafeHtml.fromUnsafeMarkdown(rawContent)
        _renderedContent = Some(rendered)
        rendered
    }
  }

  def withRawContent(content: String): A = mutatingCopy{ c =>
    c._rawContent = Later(content)
    c._renderedContent = None
  }
  def withRawContent(content: Later[String]): A = mutatingCopy(_._rawContent=content)

  protected def setMutableMarkdownContent(rawContent: Option[Later[String]],
                                          renderedContent: Option[SafeHtml]): Unit = {
    _renderedContent = renderedContent
    _rawContent = rawContent.getOrElse(Later(new String()))
  }
}

class Post private (_inId: Option[StringId] = None,
                    _inTitle: Option[Title] = None,
                    _inSlug: Option[Slug] = None,
                    _inCreationTime: Option[Instant] = None,
                    _inPublicationStatus: Option[PublicationStatus] = None,
                    _inRawContent: Option[Later[String]] = None,
                    _inRenderedContent: Option[SafeHtml] = None,
                    _inImages: Seq[Image] = Seq.empty,
                    _inPrice: Option[Price] = None)
    extends ImageHolder[Post] with MarkdownContentHolder[Post] with AttachmentHolder[Post]{
  setMutableImageHolderImages(_inImages)
  setMutableMarkdownContent(_inRawContent, _inRenderedContent)
  val id: StringId = _inId.getOrElse(StringId.random)

  private var _price = _inPrice
  def price: Option[Price] = _price
  def priceStr: String = price.map(_.baseValue.toString).getOrElse("")
  def withPrice(price: Price): Post = mutatingCopy(_._price=Some(price))

  private var _title = _inTitle.getOrElse(Title(""))
  def title: Title = _title
  def withTitle(title: Title): Post = {
    val post = new Post(this)
    post._title = title
    post._slug = Slug(title.value)
    post
  }

  private var _slug: Slug = _inSlug.getOrElse(Slug.empty)
  def slug: Slug = _slug
  def withSlug(slug: Slug): Post = {
    val post = new Post(this)
    post._slug = slug
    post
  }

  private var _creationTime = _inCreationTime.getOrElse(Post.strictMonotonicInstant)
  def creationTime: Instant = _creationTime
  def withCreationTime(ct: Instant): Post = {
    val post = new Post(this)
    post._creationTime = ct
    post
  }

  private var _publicationStatus = _inPublicationStatus.getOrElse(Unpublished)
  def publicationStatus: PublicationStatus = _publicationStatus
  def withPublicationStatus(status : PublicationStatus): Post = {
    val post = new Post(this)
    post._publicationStatus = status
    post
  }
  def togglePublicationStatus: Post = withPublicationStatus(_publicationStatus.toggle)


  override def toString: String = f"Post(${this.id.value},${this.slug.value})"

  override def equals(that: Any): Boolean =
    that match {
      case that: Post => this.id.equals(that.id) &&
        this.title.equals(that.title) &&
        this.slug.equals(that.slug) &&
        this.creationTime.getEpochSecond == that.creationTime.getEpochSecond &&
        this.renderedContent.equals(that.renderedContent) &&
        this.publicationStatus.equals(that.publicationStatus) &&
        this.price.equals(that.price)
      case _ => false
    }

  override def hashCode: Int =  this.id.value.hashCode


  override def copyMe(): Post = new Post(this)
  private def this(post: Post) {
    this(_inId = Some(post.id), _inTitle = Some(post.title), _inSlug = Some(post.slug),
         _inCreationTime = Some(post.creationTime), _inPublicationStatus = Some(post.publicationStatus),
         _inRawContent = Some(post._possiblyNotEvaluatedRawContent),
         _inRenderedContent = post._possiblyNotEvaluatedRenderedContent,
         _inImages = post.images,
         _inPrice = post.price)
  }

}

object Post {

  def apply(): Post = new Post(_inId = Some(StringId.random))
  def apply(id: StringId): Post = new Post(_inId = Some(id))

  def apply(id: StringId, title: Title, slug: Slug, creationTime: Instant, rawContent: Later[String],
            renderedContent: SafeHtml, publicationStatus: PublicationStatus, price: Float): Post = {
    new Post(_inId = Some(id), _inTitle = Some(title), _inSlug = Some(slug), _inCreationTime = Some(creationTime),
             _inRenderedContent = Some(renderedContent), _inRawContent = Some(rawContent),
             _inPublicationStatus = Some(publicationStatus), _inPrice = Price.getOpt(price))
  }



  private var _instant = Instant.now()
  private def strictMonotonicInstant: Instant = _instant.synchronized{
    val now = Instant.now()
    val diff = now.toEpochMilli - _instant.toEpochMilli
    _instant = if(diff <= 0) {
      Instant.ofEpochMilli(now.toEpochMilli + Math.abs(diff) + 1)
    } else {
      now
    }
    Instant.ofEpochMilli(_instant.toEpochMilli)
  }
}
