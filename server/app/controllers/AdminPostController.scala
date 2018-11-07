package controllers

import java.io.File

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.files._
import com.hdigiorgi.showPhoto.model.post.{Post, PostManager}
import filters.{Admin, LanguageFilterSupport}
import javax.inject.Inject
import play.api.libs.Files
import play.api.{Configuration, Logger}
import play.api.libs.json.{Json, Reads}
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter

import scala.util.{Failure, Success, Try}

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {
  implicit val logger: Logger = Logger(this.getClass)

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Admin {Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }}

  def create() = Admin { Action { implicit request: Request[AnyContent] =>
    val post = PostManager().firsPostIfUnpublishedCreateNewOtherwise()
    Redirect(routes.AdminPostController.edit(post.id))
  }}

  def edit(id: String) = Admin {Action { implicit request: Request[AnyContent] =>
    DBInterface().post.read(id) match {
      case None => NotFound("")
      case Some(post) =>
        val imagesIds = FileSystemInterface.get.image.getStoredImages(StringId(id)).map(_.id)
        Ok(views.html.admin.post.edit(post, imagesIds))
          .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
    }
  }}

  def saveTitle(postId: String): Admin[AnyContent] =
    updateFrom[String]("post-title", PostManager().saveTitle(postId, _))

  def saveContent(postId: String): Admin[AnyContent] =
    updateFrom[String]("post-content", PostManager().saveContent(postId, _))

  def imageProcess(id: String): Admin[MultipartFormData[Files.TemporaryFile]] =
    multipart.receiveMultipart(parse, new multipart.PostImageReceiver(id))

  def imageList(postId: String): Admin[AnyContent] =
    multipart.listUploaded(new multipart.PostImageLister(postId))

  def imageDelete(id: String, imageId: String) = Admin { Action { request =>
    val fsi = FileSystemInterface.get.image
    if (!fsi.deleteImage(StringId(id), FileSlug.noSlugify(imageId))) {
      NotFound(id)
    } else {
      Ok(id)
    }
  }}

  def imageLoad(id: String, load: String) = Admin { Action { _ =>
    val fsi = FileSystemInterface.get.image
    fsi.getImageFileWithSuggestedSize(StringId(id), SmallSize, FileSlug.noSlugify(load)) match {
      case None => NotFound(load)
      case Some((imageFile,_)) => DownloadHelper.getInlineResult(imageFile)
    }
  }}


  def attachmentProcess(id: String) = Admin {Action(parse.multipartFormData(50000)) { request =>
    val fsi = FileSystemInterface.get.attachment
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    fsi.addFile(StringId(id), receivedFile, receivedFileName) match {
      case Failure(e) =>
        logger.error("when adding attachment", e)
        InternalServerError(e.getMessage)
      case Success(value) =>
        Ok(Json.obj("success" -> true,
          "newUuid" -> value.name,
        ))
    }
  }}

  def attachmentDelete(id: String, file: String) = Admin { Action { _ =>
    val fsi = FileSystemInterface.get.attachment
    fsi.removeFile(StringId(id), file) match {
      case Failure(e) =>
        logger.error("when deleting file of attachment", e)
        InternalServerError(e.getMessage)
      case Success(removed) =>
        Ok(removed.name)
    }
  }}

  def attachmentList(id: String) = Admin { Action { _ =>
    val fsi = FileSystemInterface.get.attachment
    fsi.listFiles(StringId(id)) match {
      case Failure(e) =>
        logger.error("when listing attachment", e)
        InternalServerError(e.getMessage)
      case Success(files) =>
        attachmentListToStatus(files)
    }
  }}

  private def attachmentListToStatus(fes: Seq[FileEntry]): Result = {
    val map = fes.map{fe => Map(
      "name" -> fe.name,
      "uuid" -> fe.name
    )}
    Ok(Json.toJson(map))
  }

  def publicationStatus(id: String): Admin[AnyContent] = updateFrom[Boolean]("post-publication-status", {
    case true => PostManager().publish(id)
    case false => PostManager().unpublish(id)
  })

  def delete(id: String) = Admin { Action {implicit req =>
    simpleResponse(PostManager().delete(id))
  }}

  private def updateFrom[A](field: String, savef: A => Either[ErrorMessage, _])(implicit read : Reads[A]) =
    Admin {Action { implicit request: Request[AnyContent] =>
      Try((request.body.asJson.get \ field).as[A]) match {
        case Failure(e) =>
          logger.error("can't parse request", e)
          BadRequest(e.getMessage)
        case Success(content) =>
          val opResult = savef(content)
          simpleResponse(opResult)
      }
    }}

  private def simpleResponse(r: Either[ErrorMessage, _])(implicit i18n: play.api.i18n.Messages): Result = {
    r match {
      case Left(msg) => Conflict(msg.message)
      case Right(_) => Ok("{}")
    }
  }

}
