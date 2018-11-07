package controllers

import java.io.File

import com.hdigiorgi.showPhoto.model.post.PostManager
import com.hdigiorgi.showPhoto.model.{ErrorMessage, FileSlug, Image, StringId}
import filters.{Admin, LanguageFilterSupport}
import play.api.{Configuration, Logger}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.api.mvc.Results._

package object multipart {
  object Limits {
    val maxUploadImageSize: Long = 100 * 1024 * 1024 // 100MB
    val maxUploadAttachmentSize: Long = 1024 * 1024 * 1024 // 1GB
  }

  trait MultipartReceiver[A]{
    val maxLengthBytes: Long
    def process(file: File, slug: FileSlug): Either[ErrorMessage, A]
    def description(element: A): FileDescription
  }

  trait MultipartLister {
    def descriptions: Seq[FileDescription]
  }

  trait MultiPartDeleter {
    def delete(): Either[ErrorMessage, Unit]
  }

  case class FileDescription(name: String,
                             uuid: String,
                             thumbnail: Option[Call]){
    def toJson: JsObject = {
      val basejson = Json.obj(
        "success" -> true,
        "name" -> name,
        "uuid" -> uuid,
        "newUuid" -> uuid
      )
      val finalJson= thumbnail match {
        case None => basejson
        case Some(thumnail) =>
          basejson ++ Json.obj("thumbnailUrl" -> thumnail.url)
      }
      finalJson
    }
  }
  object FileDescription{
    def apply(image: Image, thumbnail: Option[Call]): FileDescription = {
      FileDescription(name = image.fileSlug.value,
        uuid = image.id,
        thumbnail = thumbnail)
    }
  }

  class PostImageReceiver(postId: String)(implicit conf : Configuration)
      extends MultipartReceiver[Seq[Image]] {
    override val maxLengthBytes: Long =
      Limits.maxUploadImageSize

    override def process(file: File, slug: FileSlug): Either[ErrorMessage, Seq[Image]] =
      PostManager().processImage(postId, file, slug)

    override def description(elements: Seq[Image]): FileDescription = {
      FileDescription(elements.head,
        Some(routes.AdminPostController.imageLoad(postId, elements.head.id)))
    }
  }

  class PostImageLister(postId: String)(implicit conf: Configuration) extends MultipartLister {
    override def descriptions: Seq[FileDescription] = {
      PostManager().listStoredImages(StringId(postId)) map {image =>
        FileDescription(image,
          Some(routes.AdminPostController.imageLoad(postId, image.id)))
      }
    }
  }

  def receiveMultipart[A](parse: PlayBodyParsers, receiver: MultipartReceiver[A])
                                 (implicit logger: Logger) = Admin {
    Action(parse.multipartFormData(receiver.maxLengthBytes)) { request =>
      implicit val i18n: Messages = LanguageFilterSupport.messagesFromRequest(request)
      val receivedFileData = request.body.files.head
      val receivedFileName = receivedFileData.filename
      val receivedFile = receivedFileData.ref.path.toFile
      logger.info(f"processing '$receivedFileName' multipart file")
      val processed = receiver.process(receivedFile, FileSlug(receivedFileName))
      processed match {
        case Left(errorMessage) =>
          logger.error(errorMessage.id)
          InternalServerError(Json.obj(
            "success" -> false,
            "reason" -> errorMessage.message()
          ))
        case Right(value) =>
          Ok(receiver.description(value).toJson)
      }
    }
  }

  def listUploaded(lister: MultipartLister) = Admin {
    Action {
      val data = lister.descriptions map { _.toJson}
      Ok(Json.toJson(data))
    }
  }

}
