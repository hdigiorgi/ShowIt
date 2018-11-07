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
    PostManager().adminGetPostById(id) match {
      case None => NotFound(id)
        case Some(post) =>
        Ok(views.html.admin.post.edit(post))
          .withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
    }
  }}

  def saveTitle(postId: String): Admin[AnyContent] =
    updateFrom[String]("post-title", PostManager().saveTitle(postId, _))

  def saveContent(postId: String): Admin[AnyContent] =
    updateFrom[String]("post-content", PostManager().saveContent(postId, _))

  def imageProcess(id: String): Admin[MultipartFormData[Files.TemporaryFile]] =
    multipart.receiveMultipart(parse, Action, new multipart.post.ImageReceiver(id))

  def imageList(postId: String): Admin[AnyContent] =
    multipart.listUploaded(Action, new multipart.post.ImageLister(postId))

  def imageDelete(postId: String, imageId: String): Admin[AnyContent] =
    multipart.deleteUploaded(Action, new multipart.post.ImageDeleter(postId, imageId))

  def imageLoad(postId: String, imageId: String): Admin[AnyContent] =
    multipart.previewUpload(Action, new multipart.post.ImagePreviewer(postId, imageId))

  def attachmentProcess(id: String): Admin[MultipartFormData[Files.TemporaryFile]] =
    multipart.receiveMultipart(parse, Action, new multipart.post.AttachmentReceiver(id))

  def attachmentDelete(id: String, file: String): Admin[AnyContent] =
    multipart.deleteUploaded(Action, new multipart.post.AttachmentDeleter(id, file))

  def attachmentList(id: String): Admin[AnyContent] =
    multipart.listUploaded(Action, new multipart.post.AttachmentLister(id))

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
