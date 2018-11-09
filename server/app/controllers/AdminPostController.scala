package controllers

import java.io.File

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.files._
import com.hdigiorgi.showPhoto.model.post.{Post, PostManager}
import filters.{Loged, LanguageFilterSupport}
import javax.inject.Inject
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager
import play.api.libs.Files
import play.api.Configuration
import play.api.libs.json.{Json, Reads}
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter

import scala.util.{Failure, Success, Try}

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {
  implicit val logger: Logger = LogManager.getLogger(this.getClass)

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Loged {Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }}

  def create() = Loged { Action { implicit request: Request[AnyContent] =>
    val post = PostManager().firsPostIfUnpublishedCreateNewOtherwise()
    Redirect(routes.AdminPostController.edit(post.id))
  }}

  def edit(id: String) = Loged {Action { implicit request: Request[AnyContent] =>
    PostManager().adminGetPostById(id) match {
      case None => NotFound(id)
        case Some(post) =>
        Ok(views.html.admin.post.edit(post))
          .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
    }
  }}

  def saveTitle(postId: String): Loged[AnyContent] =
    argumented.updateFrom[String](Action,"post-title", PostManager().saveTitle(postId, _))

  def saveContent(postId: String): Loged[AnyContent] =
    argumented.updateFrom[String](Action, "post-content", PostManager().saveContent(postId, _))

  def imageProcess(id: String): Loged[MultipartFormData[Files.TemporaryFile]] =
    multipart.receiveMultipart(parse, Action, new multipart.post.ImageReceiver(id))

  def imageList(postId: String): Loged[AnyContent] =
    multipart.listUploaded(Action, new multipart.post.ImageLister(postId))

  def imageDelete(postId: String, imageId: String): Loged[AnyContent] =
    multipart.deleteUploaded(Action, new multipart.post.ImageDeleter(postId, imageId))

  def imageLoad(postId: String, imageId: String): Loged[AnyContent] =
    multipart.previewUpload(Action, new multipart.post.ImagePreviewer(postId, imageId))

  def attachmentProcess(id: String): Loged[MultipartFormData[Files.TemporaryFile]] =
    multipart.receiveMultipart(parse, Action, new multipart.post.AttachmentReceiver(id))

  def attachmentDelete(id: String, file: String): Loged[AnyContent] =
    multipart.deleteUploaded(Action, new multipart.post.AttachmentDeleter(id, file))

  def attachmentList(id: String): Loged[AnyContent] =
    multipart.listUploaded(Action, new multipart.post.AttachmentLister(id))

  def publicationStatus(id: String): Loged[AnyContent] =
    argumented.updateFrom[Boolean](Action,"post-publication-status", {
      case true => PostManager().publish(id)
      case false => PostManager().unpublish(id)
    })

  def delete(id: String) = Loged { Action { implicit req =>
    argumented.simpleResponse(PostManager().delete(id))
  }}



}
