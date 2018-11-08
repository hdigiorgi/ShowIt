package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.external.fineUploader._
import com.hdigiorgi.showit.utils._
import org.scalajs.dom

import scala.scalajs.js

case class Uploader(wrapperId: String, listReadyCallback: Function0[Unit] = () => ()) {
  private val wrapper = `$#`(wrapperId, "upload-form-group")
  private val element = `$$0`(wrapper, ".uploader")
  private val processUrl = `!attr`(element, "process")
  private val sessionEndpoint = `!attr`(element, "list")
  private val notifier = SimpleTextInformer.fromElement(`$#`(wrapperId,"upload-form-group-notifier"))
  private var deleteErrorNotificationTimeout: Int = _

  this.createUploader()

  private def createUploader(): FineUploader = {
    val deleteOpt = DeleteFileOpt(Enabled = true, Endpoint = f"$processUrl&delete=")
    val callbacks = CallbacksOpt(
      onSessionRequestComplete = () => onUploaderSessionComplete(),
      onError = onUploaderError
    )
    val opts = CreationOptions(
      Element = element.get(0),
      Request = RequestOpt(processUrl),
      DeleteFile = deleteOpt,
      Retry = RetryOpt(EnableAuto = false, ShowButton= true),
      Session = SessionOpt(sessionEndpoint),
      Callbacks = callbacks)
    FineUploaderSupportedFeatures.imagePreviews = false
    new FineUploader(opts)
  }

  private def onUploaderSessionComplete(): Unit = {
    listReadyCallback()
  }

  private def onUploaderError(_fileId: Integer, fileName: String, _errorReason: String, xhr: dom.XMLHttpRequest): Unit = {
    dom.window.clearTimeout(this.deleteErrorNotificationTimeout)
    notifier.informError(Some(xhr.responseText))
    this.deleteErrorNotificationTimeout = dom.window.setTimeout(() => notifier.informHide(), 8000)

  }
}

