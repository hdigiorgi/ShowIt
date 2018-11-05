package controllers

import filters.{Admin, AuthenticationSupport, LanguageFilterSupport}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}

class AdminSiteController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends AbstractController(cc) with LanguageFilterSupport with AuthenticationSupport {

  def index() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def saveTitle() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def saveDescription() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def saveDomain() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def saveSocialLinks() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def imageProcess(id: String)() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def imageDelete(id: String, delete: String)() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def imageLoad(id: String, load: String)() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}
  def imageList(id: String)() = Admin { Action {
    Ok(views.html.admin.site.edit())
  }}

}
