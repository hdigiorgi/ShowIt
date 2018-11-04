package com.hdigiorgi.showit

import org.scalajs.dom
import scala.scalajs.js.RegExp

object Utils {
  def whenMobile(body: => Any): Any = {
    if(isMobile()) body
  }

  def isMobile(): Boolean = {
    new RegExp("Mobi").test(dom.window.navigator.userAgent)
  }
}
