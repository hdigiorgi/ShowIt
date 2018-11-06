package controllers

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.files._
import com.hdigiorgi.showPhoto.model.post.{Post, PostManager}
import filters.{Admin, LanguageFilterSupport}
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter
import scala.util.{Failure, Success, Try}

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {
  private val maxUploadImageSize = 50 * 1024 * 1024 // 50MB
  private val maxUploadAttachmentSize = 1024 * 1024 * 1024 // 1GB
  val logger: Logger = Logger(this.getClass)

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
    saveFromString(postId, "post-title", PostManager().saveTitle(postId, _))

  def saveContent(postId: String): Admin[AnyContent] =
    saveFromString(postId, "post-content", PostManager().saveContent(postId, _))

  private def saveFromString(postId: String, field: String, savef: String => Either[ErrorMessage, _]) =
    Admin {Action { implicit request: Request[AnyContent] =>
    Try((request.body.asJson.get \ field).as[String]) match {
      case Failure(e) =>
        logger.error("can't parse request", e)
        BadRequest(e.getMessage)
      case Success(content) =>
        savef(content) match {
          case Left(msg) => Conflict(msg.message)
          case Right(_) => Ok("{}")
        }
    }
  }}

  def imageProcess(id: String) = Admin {Action(parse.multipartFormData(maxUploadImageSize)) { request =>
    val fsi = FileSystemInterface.get.image
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    val processResult = fsi.process(receivedFile, StringId(id), FileSlug(receivedFileName))
    getProcessStatus(id, fsi, processResult)
  }}

  private def getProcessStatus(resourceId: String, fsi: ImageFileDB, pr : Try[Seq[Image]]): Result = pr match {
    case Failure(t) =>
      logger.error("processing error", t)
      InternalServerError(Json.obj(
        "success" -> false
      ))
    case Success(images) =>
      val image = images(0)
      logger.debug(f"image ${image.id} uploaded")
      Ok(Json.obj("success" -> true,
        "newUuid" -> image.id,
        "thumbnailUrl" -> routes.AdminPostController.imageLoad(resourceId, image.id).url
      ))
  }

  def imageList(postId: String) = Admin {Action { request =>
    val images = FileSystemInterface.get.image.getStoredImages(StringId(postId))
    val data = images.map(images => {
      Map(
        "name" -> images.fileSlug.value,
        "uuid" -> images.id,
        "thumbnailUrl" -> routes.AdminPostController.imageLoad(postId, images.id).url
      )
    })
    Ok(Json.toJson(data))
  }}


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


  def attachmentProcess(id: String) = Admin {Action(parse.multipartFormData(maxUploadImageSize)) { request =>
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

  def publish(id: String) = Admin { Action {implicit req =>
    simpleResponse(PostManager().publish(id))
  }}

  def unpublish(id: String) = Admin { Action {implicit req =>
    simpleResponse(PostManager().unpublish(id))
  }}

  def delete(id: String) = Admin { Action {implicit req =>
    simpleResponse(PostManager().delete(id))
  }}

  private def simpleResponse(r: Either[ErrorMessage, Post])(implicit i18n: play.api.i18n.Messages): Result = {
    r match {
      case Left(msg) => Conflict(msg.message)
      case Right(_) => Ok("{}")
    }
  }




}
