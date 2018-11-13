package controllers

import java.util.concurrent.Executors

import com.hdigiorgi.showPhoto.model.site.{Site, SiteManager}
import filters.{AuthenticationSupport, LanguageFilterSupport, TrackingSupport}
import org.apache.logging.log4j.{LogManager, Logger}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

abstract class BaseController(cc: ControllerComponents)(implicit conf : Configuration)
    extends AbstractController(cc) with LanguageFilterSupport
    with AuthenticationSupport with TrackingSupport {
  protected implicit val logger: Logger = LogManager.getLogger(this.getClass)
  protected lazy val siteManager = SiteManager()
  protected def site: Site = siteManager.site

  protected def printableRequest[A](request: Request[A]): String = {
    "\npath: " + f"(${request.method})" + request.path + "\n" +
    "query: " + request.queryString.toSeq.toString() + "\n" +
    "body: " + request.body + "\n"
  }

  implicit val executionContext : ExecutionContext = BaseController.executionContext
}

object BaseController {
  val executionContext : ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newCachedThreadPool())
}
