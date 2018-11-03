package com.hdigiorgi.showit.pages

import org.scalajs.jquery.jQuery

class Post {
  def run(): Unit = jQuery {
    updateNavbarColors()
    jQuery("#imageCarousel").on("slid.bs.carousel", () => updateNavbarColors())
  }

  private def updateNavbarColors(): Unit = {
    val img = jQuery("#imageCarousel .active img").get(0)
    val foreground = img.getAttribute("foreground")
    val background = img.getAttribute("background")
    val navbar =jQuery("#postNavbar")
    navbar.css("color", foreground)
    navbar.css("background-color", background)
  }
}
