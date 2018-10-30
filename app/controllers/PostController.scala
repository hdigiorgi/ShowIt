package controllers

import com.google.inject.Singleton
import com.hdigiorgi.showPhoto.model.files.SmallSize
import com.hdigiorgi.showPhoto.model.post.{Post, PostManager, PostWithImages}
import filters.{AuthenticationSupport, LanguageFilterSupport}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._


@Singleton
class PostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {

  def index(page: Int) = Action {
    val postData: Seq[PostWithImages] = PostManager().postsWithImageIds(page)
    Ok(views.html.index(postData))
  }

  def image(postId: String, size: String, imageName: String) = Action { implicit r =>
    PostManager().image(postId, size, imageName) match {
      case Left(errorMessage) => NotFound(errorMessage.message())
      case Right(file) => DownloadHelper.getInlineResult(file)
    }
  }

  def smallImage(postId: String, imageName: String): Action[AnyContent] = image(postId, SmallSize.name, imageName)

  def post(slug: String) = Action { _ =>
    Ok("post")
  }


}

