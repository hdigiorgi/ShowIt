package controllers

import filters.{LanguageFilterSupport, Admin}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class AdminController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport {

  def index() = Admin {
    Action { implicit request: Request[AnyContent] =>
      Ok(views.html.admin.index())
    }
  }

}
