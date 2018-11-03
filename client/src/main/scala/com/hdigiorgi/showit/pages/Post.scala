package com.hdigiorgi.showit.pages

import com.hdigiorgi.showit.external.{Viewer, ViewerOptions}
import org.scalajs.dom
import org.scalajs.jquery.jQuery

import scala.scalajs.js

class Post {
  def run(): Unit = jQuery {
    updateNavbarColors()
    jQuery("#imageCarouselZoom").on("click", () => zoomImage())
    jQuery("#imageCarousel").on("slid.bs.carousel", () => updateNavbarColors())
  }

  private def updateNavbarColors(): Unit = {
    val img = getActiveImg()
    val foreground = img.getAttribute("foreground")
    val background = img.getAttribute("background")
    val navbar =jQuery("#postNavbar")
    navbar.css("color", foreground)
    navbar.css("background-color", background)
  }

  private def zoomImage(): Unit = {
    val img = getActiveImg()
    val fullSizeUrl = img.getAttribute("fullSizeUrl")
    val viewer = getViewer(img, fullSizeUrl)
    viewer.show()
    js.Dynamic.global.img = img
    js.Dynamic.global.url = fullSizeUrl
    js.Dynamic.global.v = viewer
  }

  private def getViewer(img: dom.Element, url: String): Viewer = {
    viewers.get(url) match {
      case Some(viewer) => viewer
      case None =>
        val viewer = new Viewer(img, ViewerOptions("fullSizeUrl"))
        viewers(url) = viewer
        viewer
    }
  }

  private def getActiveImg(): dom.Element = jQuery("#imageCarousel .active img").get(0)

  private var viewers = collection.mutable.Map[String, Viewer]()
}
