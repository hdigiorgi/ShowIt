package controllers

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import com.hdigiorgi.showPhoto.model.StringId
import com.hdigiorgi.showPhoto.model.post.{ImageSizeType, Post}
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
    Ok(views.html.admin.post.edit(testPost))
      .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
  }

  def save(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def imageProcess(id: String) = WhenAdmin { Action(parse.multipartFormData) { request =>
    val receivedFileData = request.body.files.head
    val receivedFileName = receivedFileData.filename
    val receivedFile = receivedFileData.ref.path.toFile
    val finalLocation = Post.getImage(StringId(id), ImageSizeType.Original, receivedFileName)
    FileUtils.forceMkdirParent(finalLocation)
    FileUtils.moveFile(receivedFile, finalLocation)
    Ok("ok")
  }}

  def imageDelete(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}


  def imageRevert(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageLoad(id: String, load: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageRestore(id: String, restore: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageFetch(id: String, fetch: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

}
