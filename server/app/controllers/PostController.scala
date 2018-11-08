package controllers

import com.google.inject.Singleton
import com.hdigiorgi.showPhoto.model.files.SmallSize
import com.hdigiorgi.showPhoto.model.post.PostManager
import filters.LanguageFilterSupport
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter


@Singleton
class PostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {

  def index(page: Option[Int] = None) = Action {
    val posts = PostManager().publishedPosts(page.getOrElse(0))
    Ok(views.html.index(posts))
  }

  def image(postId: String, size: String, imageName: String) = Action { implicit r =>
    PostManager().getImageFile(postId, size, imageName) match {
      case Left(errorMessage) => NotFound(errorMessage.message())
      case Right(imageFile) => DownloadHelper.getInlineResult(imageFile)
    }
  }

  def smallImage(postId: String, imageName: String): Action[AnyContent] = image(postId, SmallSize.name, imageName)

  def post(slug: String) = Action { _ =>
    PostManager().post(slug) match {
      case None => Redirect(routes.PostController.index(None))
      case Some(post) => Ok(views.html.post(post)).withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
    }
  }

}

