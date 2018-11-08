package controllers

import filters.{Loged, AuthenticationSupport, LanguageFilterSupport}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}

class AdminSiteController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport with AuthenticationSupport {

  def index() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveTitle() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveDescription() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveDomain() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def saveSocialLinks() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageProcess()() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageDelete(delete: String)() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageLoad(load: String)() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}
  def imageList()() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit())
  }}

}
