package controllers

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import com.hdigiorgi.showPhoto.model.{FileSlug, Slug, StringId}
import com.hdigiorgi.showPhoto.model.files.FileSystemInterface
import com.hdigiorgi.showPhoto.model.post.Post
import filters.WhenAdmin
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter
import org.apache.commons.io.FileUtils


class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AdminPostController.save("some_test_id"))
  }}

  def edit(id: String) = Action { implicit request: Request[AnyContent] =>
    views.html.helper.form
    val testPost = Post.empty(StringId(id))
    val images = FileSystemInterface.get.image.getStoredFileNames(StringId(id))
    Ok(views.html.admin.post.edit(testPost, images))
      .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
  }

  def save(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def imageProcess(id: String) = WhenAdmin { Action(parse.multipartFormData) { request =>
    val fsi = FileSystemInterface.get.image
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    val destinationFile = fsi.moveEnsureNew(receivedFile, StringId(id), FileSlug(receivedFileName))
    Ok(destinationFile.getName)
  }}

  def imageDelete(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}


  def imageRevert(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageLoad(id: String, load: String) = WhenAdmin { Action { _ =>
    val fsi = FileSystemInterface.get.image
    val file = fsi.getFile(StringId(id), FileSlug(load))
    if(!file.exists()) NotFound("") else {
      DownloadHelper.getInlineResult(file)
    }
  }}

  def imageRestore(id: String, restore: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageFetch(id: String, fetch: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

}
