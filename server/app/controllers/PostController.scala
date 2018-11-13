package controllers

import com.google.inject.Singleton
import com.hdigiorgi.showPhoto.model.files.SmallSize
import com.hdigiorgi.showPhoto.model.payments.paypal.BuyFormData
import com.hdigiorgi.showPhoto.model.post.PostManager
import com.hdigiorgi.showPhoto.model.purchase.PurchaseManager
import filters.{LanguageFilterSupport, TrackingSupport}
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter


@Singleton
class PostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends BaseController(cc) {

  def index(page: Option[Int] = None) = Action {
    val posts = postManager.publishedPosts(page.getOrElse(0))
    Ok(views.html.post.index(site, posts))
  }

  def image(postId: String, size: String, imageName: String) = Action { implicit r =>
    postManager.getImageFile(postId, size, imageName) match {
      case Left(errorMessage) => NotFound(errorMessage.message())
      case Right(imageFile) => DownloadHelper.getInlineResult(imageFile)
    }
  }

  def smallImage(postId: String, imageName: String): Action[AnyContent] = image(postId, SmallSize.name, imageName)

  def post(slug: String) = Action { implicit request =>
    postManager.post(slug) match {
      case None => Redirect(routes.PostController.index(None))
      case Some(post) => Ok(views.html.post.post(post, BuyFormData(site, post, purchaseManager)))
    }
  }

  def downloadCancelled(postId: String) = Action {
    postManager.postById(postId) match {
      case None => Redirect(routes.PostController.index(None))
      case Some(post) => Redirect(routes.PostController.post(post.slug.value))
    }
  }

  def waitForDownload(postId: String) = Action { implicit request =>
    postManager.postById(postId) match {
      case None => Redirect(routes.PostController.index(None))
      case Some(post) =>
        Ok(views.html.post.waitForDownload(site, post))
    }
  }

  def getDownloadLink(postId: String) = Action { implicit request =>
    postManager.getAttachment(postId) match {
      case Left(message) =>
        val tracking = request2TrackingHolder(request).trackingToString()
        logger.info(f"no download available for id $postId with tracking $tracking and reason ${message.id}")
        Conflict(message.message())
      case Right(file) =>
        Ok(Json.obj(
          "url" -> routes.PostController.doDownloadFile(postId).url
        ))
    }
  }

  def doDownloadFile(postId: String) = Action { implicit request =>
    argumented.fileResponse(postManager.getAttachment(postId))
  }

  private val postManager = PostManager()
  private val purchaseManager = postManager.purchaseManager
}

