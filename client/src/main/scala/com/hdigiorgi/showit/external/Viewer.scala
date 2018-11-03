package com.hdigiorgi.showit.external

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSName, ScalaJSDefined}

@ScalaJSDefined
trait ViewerOptions extends js.Object {
  val url: String
  val navbar: Boolean
  val title: Boolean
  val toolbar: Boolean
  val show: js.Function0[Unit]
  val hidden: js.Function0[Unit]
}
object ViewerOptions {
  def apply(url: String, navbar: Boolean = false, title: Boolean = false, toolbar: Boolean = false,
            show: scala.Function0[Unit] = () => (), hidden: scala.Function0[Unit] = () => ()): ViewerOptions = {
    val _url = url
    val _navbar = navbar
    val _title = title
    val _toolbar = toolbar
    val _show = show
    val _hidden = hidden
    js.use[ViewerOptions]( new ViewerOptions {
      override val url: String = _url
      override val navbar: Boolean = _navbar
      override val title: Boolean = _title
      override val toolbar: Boolean = _toolbar
      override val show: js.Function0[Unit] = _show
      override val hidden: js.Function0[Unit] = _hidden
    }).as[ViewerOptions]
  }

}


@js.native
@JSGlobal("Viewer")
class Viewer(image: dom.Element, options: ViewerOptions) extends js.Object {
  def show(show: Boolean = true): js.Any = js.native
}
