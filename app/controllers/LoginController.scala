package controllers

import filters.LanguageFilterSupport
import javax.inject._
import play.api._
import play.api.i18n.I18nSupport
import play.api.mvc._
import views.html.helper.CSRF

class LoginController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with LanguageFilterSupport {

  def index() = Action { implicit request: Request[AnyContent] =>
    CSRF.getToken
    Ok(views.html.login())
  }

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  def logout() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

}
