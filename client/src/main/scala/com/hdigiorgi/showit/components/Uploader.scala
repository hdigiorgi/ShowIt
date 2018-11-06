package com.hdigiorgi.showit.components
import org.scalajs.dom
import org.scalajs.jquery._
import com.hdigiorgi.showit.external.fineUploader._
import scala.scalajs.js

object Uploader {
  def create(wrapperId: String, callback: () => js.Any): Unit = {
    val wrapper = jQuery(f"#$wrapperId-upload-form-group")
    val element = wrapper.children(".uploader").get(0)
    val processUrl = element.getAttribute("process")
    val listUrl = element.getAttribute("list")
    createUploader(element, callback, processUrl, listUrl)
  }

  private def createUploader(element: dom.Element, callback: () => js.Any, processUrl: String, listUrl: String): Unit = {

    val deleteOpt = DeleteFileOpt(Enabled = true, Endpoint = f"$processUrl&delete=")
    val opts = CreationOptions(
      Element = element,
      Request = RequestOpt(processUrl),
      DeleteFile = deleteOpt,
      Retry = RetryOpt(EnableAuto = false, ShowButton= true),
      Session = SessionOpt(listUrl),
      Callbacks =  CallbacksOpt(callback))
    new FineUploader(opts)
  }

}
