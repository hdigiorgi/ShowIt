package com.hdigiorgi.showPhoto.integration

import com.hdigiorgi.showPhoto.IntegrationTestBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LoginIntegration extends IntegrationTestBase {

  test("login") {
    logIn()
    logOut()
  }


}
