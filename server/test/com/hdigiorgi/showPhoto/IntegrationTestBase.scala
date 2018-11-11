package com.hdigiorgi.showPhoto

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler
import controllers.routes
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{FunSuite, Matchers}

import scala.language.postfixOps
import org.scalatest.selenium._
import play.api.Application
import play.api.mvc.Call
import play.api.test.TestServer


trait IntegrationTestBase extends FunSuite with Matchers with WebBrowser with Driver{
  import IntegrationTestBase._
  protected val host = f"http://localhost:$PORT"
  protected def url(path: Call): String = f"$host${path.url}"
  protected val loginUrl: String = url(routes.AuthenticationController.login())
  protected val logoutUrl: String = url(routes.AuthenticationController.logout())
  protected val adminUrl: String = url(routes.AdminController.index())
  protected val application: Application = UnitTestBase.fakeApplication()
  override implicit val webDriver: HtmlUnitDriver = new ITWebDriver()
  initServer(application)

  protected def logIn(): Unit = {
    go to loginUrl
    if(!currentUrl.equals(adminUrl)){
      currentUrl shouldEqual loginUrl
      emailField("email-input").value = "me@hdigiorgi.com"
      pwdField("password-input").value = "password"
      click on id("login-submit-button")
      currentUrl should be (adminUrl)
    }
  }

  protected def logOut(): Unit = {
    go to logoutUrl
    go to loginUrl
    currentUrl should be (loginUrl)
  }

}

class ITWebDriver extends HtmlUnitDriver {
  this.getWebClient.setCssErrorHandler(new SilentCssErrorHandler)
  this.setJavascriptEnabled(true)
}

object IntegrationTestBase {

  protected val PORT = 6060
  protected var server: Option[TestServer] = None
  private def initServer(app: Application): Unit = server match {
    case None =>
      this.server = Some(TestServer(6060, app))
      server.get.start()
    case Some(srv) =>
      srv.start()
  }
  private def stopServer(): Unit = server match {
    case None => ()
    case Some(srv) => srv.stop()
  }
}