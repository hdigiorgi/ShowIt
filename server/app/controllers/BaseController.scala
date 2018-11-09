package controllers

import com.hdigiorgi.showPhoto.model.site.{Site, SiteManager}
import filters.{AuthenticationSupport, LanguageFilterSupport}
import org.apache.logging.log4j.{LogManager, Logger}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}

class BaseController(cc: ControllerComponents)(implicit conf : Configuration) extends
  AbstractController(cc) with LanguageFilterSupport with AuthenticationSupport {
  protected implicit val logger: Logger = LogManager.getLogger(this.getClass)
  protected lazy val siteManager = SiteManager()
  protected def site: Site = siteManager.site
}
