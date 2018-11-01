package com.hdigiorgi.showPhoto.integration

import com.hdigiorgi.showPhoto.IntegrationTestBase

class LoginIntegration extends IntegrationTestBase {
  logAsAdmin()

  test("logout") {
    logOut()
    currentUrl should be (url("/"))
    go to adminUrl
    currentUrl should be (loginUrl)
  }

  test("Show login form") {
    logAsAdmin()
    currentUrl should be (adminUrl)
    pageTitle.toLowerCase should be ("administration")
  }

}
