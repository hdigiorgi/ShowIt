package com.hdigiorgi.showPhoto


import org.scalatest.{AppendedClues, FunSuite, Matchers}

import scala.language.postfixOps
import org.scalatest.selenium.{Firefox, HtmlUnit}
import play.api.Application
import play.api.test.TestServer
import play.core.server.DevServerStart

trait IntegrationTestBase extends FunSuite with Matchers  with HtmlUnit{
  import IntegrationTestBase._
  protected val host = f"http://localhost:$PORT"
  protected def url(path: String): String = f"$host$path"
  protected val application: Application = UnitTestBase.fakeApplication()
  initServer(application)
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