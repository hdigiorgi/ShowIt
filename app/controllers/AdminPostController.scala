package controllers

import com.hdigiorgi.showPhoto.model.{FileSlug, StringId}
import com.hdigiorgi.showPhoto.model.files.{FileSystemInterface, SmallSize}
import com.hdigiorgi.showPhoto.model.post.Post
import filters.WhenAdmin
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {
  private val maxUploadImageSize = 30 * 1024 * 1024

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AdminPostController.save("some_test_id"))
  }}

  def edit(id: String) = Action { implicit request: Request[AnyContent] =>
    views.html.helper.form
    val testPost = Post.empty(StringId(id))
    val imagesIds = FileSystemInterface.get.image.getStoredImageIds(StringId(id))
    Ok(views.html.admin.post.edit(testPost, imagesIds))
      .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
  }

  def save(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def imageProcess(id: String) = WhenAdmin {Action(parse.multipartFormData(maxUploadImageSize)) { request =>
    val fsi = FileSystemInterface.get.image
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    fsi.process(receivedFile, StringId(id), FileSlug(receivedFileName)) match {
      case Left(e) => throw e
      case r => Ok(fsi.getImageId(r).get)
    }
  }}

  def imageDelete(id: String) = WhenAdmin { Action { request =>
    val fsi = FileSystemInterface.get.image
    request.body.asText match {
      case None => BadRequest("empty body")
      case Some(imageName) =>
        if (!fsi.deleteImage(StringId(id), FileSlug.noSlugify(imageName))) {
          NotFound(id)
        } else {
          Ok(id)
        }
    }
  }}

  def imageLoad(id: String, load: String) = WhenAdmin { Action { _ =>
    val fsi = FileSystemInterface.get.image
    fsi.getImageWithSuggestedSize(StringId(id), SmallSize, FileSlug(load)) match {
      case None => NotFound(load)
      case Some(file) => DownloadHelper.getInlineResult(file)
    }
  }}


}
