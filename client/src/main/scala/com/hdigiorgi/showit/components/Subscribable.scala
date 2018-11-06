package com.hdigiorgi.showit.components

import scala.scalajs.js
import org.scalajs.jquery.JQueryAjaxSettings
import org.scalajs.jquery.jQuery

import scala.scalajs.js.timers.SetTimeoutHandle

trait Subscribable[A,B] {
  val element: A
  def value: B
  def subToChange(callback: () => Unit): Unit
}

trait ResultInformer {
  def error(msg: Option[String] = None): Unit
  def working(msg: Option[String] = None): Unit
  def success(msg: Option[String] = None): Unit
}

class Updatable[A](name: String, sub: Subscribable[A, String],
                   informer: ResultInformer, endpoint: String) {
  var timeOutHandle: SetTimeoutHandle = _
  sub.subToChange(onChangeDetected)

  private def onChangeDetected(): Unit = {
    informer.working()
    js.timers.clearTimeout(timeOutHandle)
    timeOutHandle = js.timers.setTimeout(200)(sendSaveRequest())
  }

  private def sendSaveRequest(): Unit = {
    val value = sub.value
    val request = js.Dictionary(name -> value)
    val stringRequest = js.JSON.stringify(request)
    val opts = getJQueryAjaxSettingsPostJson(stringRequest)
    informer.working()
    jQuery.ajax(opts)
      .done(() => informer.success())
      .fail((failure: js.Dynamic) => informer.error(Some(failure.responseText.asInstanceOf[String])))
  }

  private def getJQueryAjaxSettingsPostJson(body: String): JQueryAjaxSettings = js.Dynamic.literal(
    `type`      = "POST",
    contentType = "application/json; charset=utf-8",
    dataType    = "json",
    url         = endpoint,
    data        = body
  ).asInstanceOf[JQueryAjaxSettings]

}