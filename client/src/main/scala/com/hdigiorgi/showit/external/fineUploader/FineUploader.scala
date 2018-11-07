package com.hdigiorgi.showit.external.fineUploader

import org.scalajs.dom
import org.scalajs.dom.Element

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel, JSGlobal}

trait RequestOpt extends js.Object{
  val endpoint: String
}
object RequestOpt {
  def apply(Endpoint: String): RequestOpt = js.use{ new RequestOpt {
    override val endpoint: String = Endpoint
  }}.as[RequestOpt]
}

trait DeleteFileOpt extends js.Object {
  val enabled: Boolean
  val endpoint: String
}
object DeleteFileOpt {
  def apply(Enabled: Boolean, Endpoint: String): DeleteFileOpt = js.use{ new DeleteFileOpt {
    override val enabled: Boolean = Enabled
    override val endpoint: String = Endpoint
  }}.as[DeleteFileOpt]
}

trait RetryOpt extends js.Object {
  val enableAuto: Boolean
  val showButton: Boolean
}
object RetryOpt {
  def apply(EnableAuto: Boolean, ShowButton: Boolean): RetryOpt = js.use{ new RetryOpt {
    override val enableAuto: Boolean = EnableAuto
    override val showButton: Boolean = ShowButton
  }}.as[RetryOpt]
}

trait SessionOpt extends js.Object{
  val endpoint: String
}
object SessionOpt {
  def apply(Endpoint: String): SessionOpt = js.use{ new SessionOpt{
    override val endpoint: String = Endpoint
  }}.as[SessionOpt]
}

trait CallbacksOpt extends js.Object{
  val onSessionRequestComplete: js.Function0[js.Any]
  val onError: js.Function3[Integer, String, String, js.Any] // file id, file name, reason
}
object CallbacksOpt {
  def apply(onSessionRequestComplete: js.Function0[js.Any],
            onError: js.Function3[Integer, String, String, js.Any]): CallbacksOpt = js.Dynamic.literal(
    onSessionRequestComplete = onSessionRequestComplete,
    onError = onError
  ).asInstanceOf[CallbacksOpt]
}

trait CreationOptions extends js.Object{
  val element: dom.Element
  val request: RequestOpt
  val deleteFile: DeleteFileOpt
  val retry: RetryOpt
  val session: SessionOpt
  val callbacks: CallbacksOpt
}
object CreationOptions {
  def apply(Element: dom.Element, Request: RequestOpt, DeleteFile: DeleteFileOpt, Retry: RetryOpt, Session: SessionOpt,
            Callbacks: CallbacksOpt): CreationOptions = js.use{new CreationOptions {
    override val element: Element = Element
    override val request: RequestOpt = Request
    override val deleteFile: DeleteFileOpt = DeleteFile
    override val retry: RetryOpt = Retry
    override val session: SessionOpt = Session
    override val callbacks: CallbacksOpt = Callbacks
  }}.as[CreationOptions]
}
@js.native
@JSGlobal("qq.FineUploader")
class FineUploader(options: CreationOptions) extends js.Object
