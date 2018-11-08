package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.external.simpleMDE.{SimpleMDE, SimpleMDECreationOptions}
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js
import com.hdigiorgi.showit.utils._

object MDEditor {
  def create(wrapperId: String): Updater[SimpleMDE, String] = {
    val wrapper  = `$#`(wrapperId, "markdown-editor-form-group")
    val element = `$$0`(wrapper, ".md-editor")
    val name = `!attr`(element, "name")
    val endpoint = `!attr`(element, "save-url")
    val smde = createSMDE(element)
    val informer = SimpleTextInformer.fromElement(`$#`(wrapperId,"input-indicator"))
    new Updater[SimpleMDE,String](name, smde, informer, endpoint)
  }

  private def createSMDE(element: JQuery): Subscribable[SimpleMDE, String]  = {
    val opts = SimpleMDECreationOptions(
      Element = element.get(0),
      SpellChecker = false,
      Toolbar = js.Array("bold", "italic", "quote", "link", "horizontal-rule","|",
        "heading","unordered-list", "ordered-list", "code", "|",
        "preview", "side-by-side", "fullscreen"),
      Status = false,
      IndentWithTabs = false
    )
    val editor = new SimpleMDE(opts)
    smdeToSubscribable(editor)
  }

  private def smdeToSubscribable(editor: SimpleMDE): Subscribable[SimpleMDE, String] = {
    new Subscribable[SimpleMDE, String] {
      override val getSubscribedElement: SimpleMDE = editor
      override def getSubscribedValue: String = editor.value()
      override def subscribeToChange(callback: () => Unit): Unit = {
        editor.codemirror.on("change", () => callback())
      }
    }
  }

}
