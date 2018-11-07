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
    def name: String
    def delete(): Either[ErrorMessage, Unit]
  }

  trait MultipartPreviewer {
    def name: String
    def preview(): Option[File]
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

  def receiveMultipart[A](parse: PlayBodyParsers, action: ActionBuilder[Request, AnyContent],
                          receiver: MultipartReceiver[A])
                          (implicit logger: Logger) = Admin {
    action(parse.multipartFormData(receiver.maxLengthBytes)) { request =>
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

  def listUploaded(action: ActionBuilder[Request, AnyContent],
                   lister: MultipartLister) = Admin {
    action {
      val data = lister.descriptions map { _.toJson}
      Ok(Json.toJson(data))
    }
  }

  def deleteUploaded(action: ActionBuilder[Request, AnyContent], deleter: MultiPartDeleter) = Admin {
    action { request =>
      implicit val i18n: Messages = LanguageFilterSupport.messagesFromRequest(request)
      deleter.delete() match {
        case Left(message) =>
          InternalServerError(message.message())
        case Right(_) =>
          Ok(deleter.name)
      }
    }
  }

  def previewUpload(action: ActionBuilder[Request, AnyContent], previewer: MultipartPreviewer) = Admin {
    action {
      previewer.preview() match {
        case None => NotFound(previewer.name)
        case Some(file) => DownloadHelper.getInlineResult(file)
      }
    }
  }

}
