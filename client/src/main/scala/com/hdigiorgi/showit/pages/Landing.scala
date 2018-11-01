package com.hdigiorgi.showit.pages

import org.scalajs.dom
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.scalajs.js

class Landing {
  def run(): Unit = jQuery(dom.window).on("load", (_: JQueryEventObject) => {
    setupMobileScrollTitles()
  })

  private def setupMobileScrollTitles(): Unit = {
    jQuery(".landing-image-grid-container .cols").on("touchstart touchend", (e: JQueryEventObject) => {
      val target = jQuery(e.currentTarget).children(".title")
      jQuery(target).toggleClass("title-hover")
    })
  }
}
