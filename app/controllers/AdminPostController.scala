package controllers

import com.hdigiorgi.showPhoto.model.{FileSlug, StringId}
import com.hdigiorgi.showPhoto.model.files.{FileSystemInterface, ImageFileDB, SmallSize}
import com.hdigiorgi.showPhoto.model.post.Post
import filters.WhenAdmin
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter
import com.hdigiorgi.showPhoto.model.files.GenericFileDB._
import org.apache.commons.io.FilenameUtils

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {
  private val maxUploadImageSize = 30 * 1024 * 1024
  val logger: Logger = Logger(this.getClass)

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = WhenAdmin {Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }}

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AdminPostController.save("some_test_id"))
  }}

  def edit(id: String) = WhenAdmin {Action { implicit request: Request[AnyContent] =>
    views.html.helper.form
    val testPost = Post.empty(StringId(id))
    val imagesIds = FileSystemInterface.get.image.getStoredImageIds(StringId(id))
    Ok(views.html.admin.post.edit(testPost, imagesIds))
      .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
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


  def attachmentProcess(id: String) = WhenAdmin { Action { _ =>
    Ok("")
  }}

  def attachmentDelete(id: String, file: String) = WhenAdmin { Action { _ =>
    Ok("")
  }}

  def attachmentList(id: String) = WhenAdmin { Action { _ =>
    Ok("")
  }}


}
