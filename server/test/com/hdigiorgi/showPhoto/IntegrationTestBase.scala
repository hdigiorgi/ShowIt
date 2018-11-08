package com.hdigiorgi.showPhoto


import com.gargoylesoftware.htmlunit.SilentCssErrorHandler
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{AppendedClues, FunSuite, Matchers, ScreenshotCapturer}

import scala.language.postfixOps
import org.scalatest.selenium._
import play.api.Application
import play.api.test.TestServer
import play.core.server.DevServerStart

trait IntegrationTestBase extends FunSuite with Matchers with WebBrowser with Driver{
  import IntegrationTestBase._
  protected val host = f"http://localhost:$PORT"
  protected def url(path: String): String = f"$host$path"
  protected val loginUrl: String = url("/login")
  protected val logoutUrl: String = url("/logout")
  protected val adminUrl: String = url("/admin")
  protected val application: Application = UnitTestBase.fakeApplication()
  override implicit val webDriver: HtmlUnitDriver = new ITWebDriver()
  initServer(application)

  protected def logIn(): Unit = {
    go to loginUrl
    if(!currentUrl.equals(adminUrl)){
      pageTitle.toLowerCase should be ("login")
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