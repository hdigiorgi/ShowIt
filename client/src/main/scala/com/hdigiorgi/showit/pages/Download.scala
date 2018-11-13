package com.hdigiorgi.showit.pages

import org.scalajs.dom
import com.hdigiorgi.showit.utils._
import org.scalajs.jquery.{JQuery, jQuery}
import scala.scalajs.js


class Download {
  private val downloadWrapper = `$#`("download-wrapper")
  private val urlToGetDownloadLink = `!attr`(downloadWrapper, "getDownloadLink")
  private val waitingDisplayElement = `$#`("waiting")
  private val errorDisplayElement = `$#`("error")
  private var retries = 10
  private val requestInterval = 3000
  getLinkNow()

  private def getLink(): Unit = {
    scala.scalajs.js.timers.setTimeout(requestInterval){
      getLinkNow()
    }
  }
  private def getLinkNow(): Unit = {
    jQuery.get(urlToGetDownloadLink, (data: js.Dynamic) => onGetLinkSuccess(data))
      .fail((data: js.Dynamic) => onGetLinkFailure(data))
  }

  private def onGetLinkSuccess(data: js.Dynamic): Unit = {
    val url = data.url.asInstanceOf[String]
    if(url == null) {
      onGetLinkFailure(data)
    } else {
      redirect(url)
    }
  }

  private def onGetLinkFailure(data: js.Dynamic): Unit = {
    `console error`(data)
    if(retries > 0) {
      retries -= 1
      getLink()
    } else {
      showDownloadFialure()
    }

  }

  private def redirect(url: String): Unit = {
    dom.window.top.location.href = url
  }

  private def showDownloadFialure(): Unit = {
    waitingDisplayElement.hide()
    errorDisplayElement.show()
  }
}
