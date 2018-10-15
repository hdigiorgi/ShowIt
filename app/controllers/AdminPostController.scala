package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {

  def index(page: Integer, order: String, search: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

  def postNew() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

  def postEdit(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

  def postSave(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

}
