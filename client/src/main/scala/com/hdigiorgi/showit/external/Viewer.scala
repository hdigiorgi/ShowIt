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
}
object ViewerOptions {
  def apply(url: String, navbar: Boolean = false, title: Boolean = true, toolbar: Boolean = false): ViewerOptions = {
    val _url = url
    val _navbar = navbar
    val _title = title
    val _toolbar = toolbar
    js.use[ViewerOptions]( new ViewerOptions {
      override val url: String = _url
      override val navbar: Boolean = _navbar
      override val title: Boolean = _title
      override val toolbar: Boolean = _toolbar
    }).as[ViewerOptions]
  }

}


@js.native
@JSGlobal("Viewer")
class Viewer(image: dom.Element, options: ViewerOptions) extends js.Object {
  def show(show: Boolean = true): js.Any = js.native
}
