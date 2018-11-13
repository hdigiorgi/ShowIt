package com.hdigiorgi.showit.pages

import com.hdigiorgi.showit.external.{Viewer, ViewerOptions}
import org.scalajs.dom
import com.hdigiorgi.showit.utils._
import org.scalajs.jquery.{JQuery, jQuery}
import scala.scalajs.js

class Post {
  private val carouselDownloadButton = `$#opt`("bellowCarouselDownloadContainer")
  private val buyButton = `$#opt`("buy-button")
  private val downloadButton = `$#opt`("download-button")
  carouselDownloadButton.map(_ =>
    jQuery(dom.window).scroll(() => checkForCarouselDownloadVisibility())
  )


  def run(): Unit = jQuery { () =>
    updateColors()
    fixCasouselSize()
    `$#`("imageCarouselZoom").on("click", () => zoomImage())
    `$#`("imageCarousel").on("slid.bs.carousel", () => updateColors())
    carouselDownloadButton.map(
      _.on("click", () => onCarouselDownloadButtonPressed())
    )
  }

  private def updateColors(): Unit = {
    val img = getActiveImg()
    val foreground = img.getAttribute("foreground")
    val background = img.getAttribute("background")
    updateElementColors(jQuery("#postNavbar"), foreground, background)
    updateElementColors(jQuery("#bellowCarouselDownload"), foreground, background)
    updateDownloadIconColor(foreground, background)
  }

  private def updateDownloadIconColor(foreground: String, background: String): Unit = {
    val e = jQuery("#bellowCarouselDownloadIcon")
    e.css("color", foreground)
    e.css("text-shadow", f"0px 0px 7px $background")
  }

  private def updateElementColors(element: JQuery, foreground: String, background: String): Unit = {
    element.css("color", foreground)
    element.css("background-color", background)
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
        val opts = ViewerOptions("fullSizeUrl", show = stopCarousel, hidden = startCarousel)
        val viewer = new Viewer(img, opts)
        viewers(url) = viewer
        viewer
    }
  }

  private def stopCarousel(): Unit = doCarouselOp("pause")
  private def startCarousel(): Unit = doCarouselOp("cycle")
  private def doCarouselOp(op: String): Unit = {
    js.Dynamic.global.jQuery("#imageCarousel").carousel(op)
  }

  private def getActiveImg(): dom.Element = jQuery("#imageCarousel .active img").get(0)

  private def fixCasouselSize(): Unit = whenMobile {
    val item1 = jQuery("#imageCarousel .carousel-item")
    val item2 = jQuery("#imageCarousel .carousel-inner")
    val viewHeight = dom.window.document.documentElement.clientHeight
    val viewWidth = dom.window.document.documentElement.clientWidth
    val targetHeight = if (viewHeight > viewWidth) {
      viewHeight * 0.85
    } else {
      viewWidth * 0.85
    }
    item1.css("height", f"${targetHeight}px")
    item1.css("max-height", f"${targetHeight}px")
    item2.css("height", f"${targetHeight}px")
    item2.css("max-height", f"${targetHeight}px")
  }

  private def downloadOrBuyButton: JQuery = buyButton.getOrElse(downloadButton.orNull)

  private def onCarouselDownloadButtonPressed(): Unit = {
    val animation = "flash-animation"
    downloadOrBuyButton.scroll()
    downloadOrBuyButton.removeClass(animation)
    dom.window.setTimeout(()=>{
      downloadOrBuyButton.addClass(animation)
    },150)
  }

  private def checkForCarouselDownloadVisibility(): Unit = {
    if(isVisibleInViewport(downloadOrBuyButton)) {
      carouselDownloadButton.map(_.css("opacity", "0"))
    } else {
      carouselDownloadButton.map(_.css("opacity", "1"))
    }
  }

  private var viewers = collection.mutable.Map[String, Viewer]()
}
