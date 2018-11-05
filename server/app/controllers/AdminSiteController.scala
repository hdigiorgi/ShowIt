package controllers

import filters.{Admin, AuthenticationSupport, LanguageFilterSupport}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}

class AdminSiteController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport with AuthenticationSupport {

  def index() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveTitle() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveDescription() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveDomain() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveSocialLinks() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageProcess()() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageDelete(delete: String)() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageLoad(load: String)() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageList()() = Admin { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}

}
