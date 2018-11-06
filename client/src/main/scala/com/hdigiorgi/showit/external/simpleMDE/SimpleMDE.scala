package com.hdigiorgi.showit.external.simpleMDE

import org.scalajs.dom
import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

trait SimpleMDECreationOptions extends js.Object {
  val element: dom.Element
  val spellChecker: Boolean
  val toolbar: js.Array[String]
  val status: Boolean
  val indentWithTabs: Boolean
}
object SimpleMDECreationOptions {
  def apply(Element: dom.Element, SpellChecker: Boolean, Toolbar: js.Array[String], Status: Boolean,
            IndentWithTabs: Boolean): SimpleMDECreationOptions = js.use{new SimpleMDECreationOptions {
    override val element: Element = Element
    override val spellChecker: Boolean = SpellChecker
    override val toolbar: js.Array[String] = Toolbar
    override val status: Boolean = Status
    override val indentWithTabs: Boolean = IndentWithTabs
  }}.as[SimpleMDECreationOptions]
}

trait SMDECodeMirror extends js.Object {
  def on(eventName: String, callback: js.Function0[Unit]): js.Any
}

@js.native
@JSGlobal("SimpleMDE")
class SimpleMDE(options: SimpleMDECreationOptions) extends js.Object {
  val codemirror: SMDECodeMirror = js.native
  def value: String = js.native
}