package controllers

import filters.WhenAdmin
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
      Ok(views.html.admin.post.edit()).withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
  }}

  def edit(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

  def save(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.list())
  }

}
