package com.hdigiorgi.showit.components

import scala.scalajs.js
import org.scalajs.jquery.JQueryAjaxSettings
import org.scalajs.jquery.jQuery

import scala.scalajs.js.timers.SetTimeoutHandle

trait Subscribable[A,B] {
  val getSubscribedElement: A
  def getSubscribedValue: B
  def subscribeToChange(callback: () => Unit): Unit
}

trait ResultInformer {
  def informError(msg: Option[String] = None): Unit
  def informWorking(msg: Option[String] = None): Unit
  def informSuccess(msg: Option[String] = None): Unit
  def informHide(): Unit = ()
}

class Updater[A,B](name: String, sub: Subscribable[A, B],
                   informer: ResultInformer, endpoint: String) {
  var timeOutHandle: SetTimeoutHandle = _
  sub.subscribeToChange(onChangeDetected)

  def cancelUpdate(): Unit = {
    js.timers.clearTimeout(timeOutHandle)
    informer.informHide()
  }

  private def onChangeDetected(): Unit = {
    informer.informWorking()
    js.timers.clearTimeout(timeOutHandle)
    timeOutHandle = js.timers.setTimeout(2000)(sendSaveRequest())
  }

  private def sendSaveRequest(): Unit = {
    val value = sub.getSubscribedValue
    val request = js.Dictionary(name -> value)
    val stringRequest = js.JSON.stringify(request)
    val opts = getJQueryAjaxSettingsPostJson(stringRequest)
    informer.informWorking()
    jQuery.ajax(opts)
      .done(() => informer.informSuccess())
      .fail((failure: js.Dynamic) => informer.informError(Some(failure.responseText.asInstanceOf[String])))
  }

  private def getJQueryAjaxSettingsPostJson(body: String): JQueryAjaxSettings = js.Dynamic.literal(
    `type`      = "POST",
    contentType = "application/json; charset=utf-8",
    dataType    = "json",
    url         = endpoint,
    data        = body
  ).asInstanceOf[JQueryAjaxSettings]

}