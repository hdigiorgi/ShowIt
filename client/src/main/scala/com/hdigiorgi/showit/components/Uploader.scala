package com.hdigiorgi.showit.components
import org.scalajs.dom
import org.scalajs.jquery._
import com.hdigiorgi.showit.external.fineUploader._
import scala.scalajs.js

object Uploader {
  def createUploader(wrapperId: String, callback: () => js.Any): Unit = {
    val wrapper = jQuery(f"#$wrapperId-form-group")
    val element = wrapper.children(".uploader").get(0)
    val processUrl = element.getAttribute("process")
    val deleteUrl = element.getAttribute("delete")
    val loadUrl = element.getAttribute("load")
    val listUrl = element.getAttribute("list")
    createUploader(element, callback, processUrl, deleteUrl, loadUrl, listUrl)
  }

  private def createUploader(element: dom.Element, callback: () => js.Any, processUrl: String,
                             deleteUrl: String, loadUrl: String, listUrl: String): Unit = {
    val opts = CreationOptions(
      Element = element,
      Request = RequestOpt(processUrl),
      DeleteFile = DeleteFileOpt(Enabled = true, Endpoint = deleteUrl),
      Retry = RetryOpt(EnableAuto = false, ShowButton= true),
      Session = SessionOpt(listUrl),
      Callbacks =  CallbacksOpt(callback))
    new FineUploader(opts)
  }

}
