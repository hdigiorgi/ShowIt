package controllers

import com.hdigiorgi.showPhoto.model.{DBInterface, FileSlug, StringId}
import com.hdigiorgi.showPhoto.model.files.{FileEntry, FileSystemInterface, ImageFileDB, SmallSize}
import com.hdigiorgi.showPhoto.model.post.{Post, PostManager}
import filters.WhenAdmin
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter
import com.hdigiorgi.showPhoto.model.files.GenericFileDB._
import org.apache.commons.io.FilenameUtils

import scala.util.{Failure, Success}

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {
  private val maxUploadImageSize = 50 * 1024 * 1024 // 50MB
  private val maxUploadAttachmentSize = 1024 * 1024 * 1024 // 1GB
  val logger: Logger = Logger(this.getClass)

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = WhenAdmin {Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }}

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
    val post = PostManager().firsPostIfUnpublishedCreateNewOtherwise()
    Redirect(routes.AdminPostController.edit(post.id))
  }}

  def edit(id: String) = WhenAdmin {Action { implicit request: Request[AnyContent] =>
    DBInterface().post.read(id) match {
      case None => NotFound("")
      case Some(post) =>
        val imagesIds = FileSystemInterface.get.image.getStoredImageIds(StringId(id))
        Ok(views.html.admin.post.edit(post, imagesIds))
          .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
    }
  }}

  def save(id: String) = WhenAdmin {Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }}

  def imageProcess(id: String) = WhenAdmin {Action(parse.multipartFormData(maxUploadImageSize)) { request =>
    val fsi = FileSystemInterface.get.image
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    val processResult = fsi.process(receivedFile, StringId(id), FileSlug(receivedFileName))
    getProcessStatus(id, fsi, processResult)
  }}

  private def getProcessStatus(resourceId: String, fsi: ImageFileDB, pr : ProcessingResult): Result = pr match {
    case Left(e) =>
      logger.error("processing error", e)
      InternalServerError(Json.obj(
        "success" -> false
      ))
    case Right(files) =>
      val imageId = fsi.getImageId(pr)
      logger.debug(f"image $imageId uploaded")
      Ok(Json.obj("success" -> true,
        "newUuid" -> imageId,
        "thumbnailUrl" -> routes.AdminPostController.imageLoad(resourceId, imageId.get).url
      ))
  }

  def imageList(postId: String) = WhenAdmin {Action { request =>
    val imagesIds = FileSystemInterface.get.image.getStoredImageIds(StringId(postId))
    val data = imagesIds.map(id => {
      Map(
        "name" -> FilenameUtils.getBaseName(id),
        "uuid" -> id,
        "thumbnailUrl" -> routes.AdminPostController.imageLoad(postId, id).url
      )
    })
    Ok(Json.toJson(data))
  }}


  def imageDelete(id: String, imageId: String) = WhenAdmin { Action { request =>
    val fsi = FileSystemInterface.get.image
    if (!fsi.deleteImage(StringId(id), FileSlug.noSlugify(imageId))) {
      NotFound(id)
    } else {
      Ok(id)
    }
  }}

  def imageLoad(id: String, load: String) = WhenAdmin { Action { _ =>
    val fsi = FileSystemInterface.get.image
    fsi.getImageWithSuggestedSize(StringId(id), SmallSize, FileSlug.noSlugify(load)) match {
      case None => NotFound(load)
      case Some(file) => DownloadHelper.getInlineResult(file)
    }
  }}


  def attachmentProcess(id: String) = WhenAdmin {Action(parse.multipartFormData(maxUploadImageSize)) { request =>
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

  def attachmentDelete(id: String, file: String) = WhenAdmin { Action { _ =>
    val fsi = FileSystemInterface.get.attachment
    fsi.removeFile(StringId(id), file) match {
      case Failure(e) =>
        logger.error("when deleting file of attachment", e)
        InternalServerError(e.getMessage)
      case Success(removed) =>
        Ok(removed.name)
    }
  }}

  def attachmentList(id: String) = WhenAdmin { Action { _ =>
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


}
