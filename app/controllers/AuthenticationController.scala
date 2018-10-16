package controllers

import com.hdigiorgi.showPhoto.model.{DBInterface, Password, User, UserPI}
import filters.{AuthenticationSupport, LanguageFilterSupport}
import javax.inject._
import play.api._
import play.api.data.{Form, Forms}
import play.api.i18n.I18nSupport
import play.api.mvc._
import views.html.helper.CSRF
import cats.syntax._
import cats.implicits._


object AuthenticationController {
  case class LoginForm(email: String, password: String)
  val loginFormMapping = Form(
    Forms.mapping(
      "email" -> Forms.text,
      "password" -> Forms.text
    )(LoginForm.apply)(LoginForm.unapply)
  )
}

class AuthenticationController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport with AuthenticationSupport{

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  def login() = Action { implicit request: Request[AnyContent] =>
    val optUser = AuthenticationController.loginFormMapping.bindFromRequest.value match {
      case None => None
      case Some(loginForm) =>
        DBInterface.wrap{ db =>
          checkLogin(loginForm, db.user)
        }
    }
    optUser match {
      case None => Unauthorized(views.html.login("authentication.login.msg.genericError".some))
      case Some(user) =>
        Redirect(routes.AdminController.index).withAuthenticated(user)
    }
  }

  def logout() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  private def checkLogin(form: AuthenticationController.LoginForm, db: UserPI): Option[User] = {
    db.readByEmail(form.email).flatMap{ user =>
      if (user.password.is(form.password)) Some(user) else None
    }
  }

}
