package controllers

import com.hdigiorgi.showPhoto.model.site.{Site, SiteManager}
import filters.{AuthenticationSupport, LanguageFilterSupport, Loged}
import javax.inject.Inject
import org.apache.logging.log4j.{LogManager, Logger}
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}

class AdminSiteController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
    extends BaseController(cc) {

  def index() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}

  def saveName(): Loged[AnyContent] =
    argumented.updateFrom[String](Action, "site-name", siteManager.updateName)

  def saveDescription(): Loged[AnyContent] =
    argumented.updateFrom[String](Action, "site-description", siteManager.updateDescription)

  def saveLinks(): Loged[AnyContent ]=
    argumented.updateFrom[Seq[String]](Action, "site-links", siteManager.updateLinks)

  def imageProcess()() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}

  def imageDelete(delete: String)() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}

  def imageLoad(load: String)() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}

  def imageList()() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}


}
