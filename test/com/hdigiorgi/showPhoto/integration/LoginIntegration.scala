package com.hdigiorgi.showPhoto.integration

import com.hdigiorgi.showPhoto.IntegrationTestBase
import play.api.test.TestServer

class LoginIntegration extends IntegrationTestBase {

  val login = url("/login")

  test("Show login form") {
    TestServer(0)
    go to login
    pageTitle.toLowerCase should be ("login")
  }
}
