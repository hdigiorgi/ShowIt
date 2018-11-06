package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.external.simpleMDE.{SimpleMDE, SimpleMDECreationOptions}
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import scala.scalajs.js

object MDEditor {
  def create(wrapperId: String): Updater[SimpleMDE, String] = {
    val wrapper  = jQuery(f"#$wrapperId-markdown-editor-form-group")
    val element = wrapper.children(".md-editor").get(0)
    val name = element.getAttribute("name")
    val endpoint = element.getAttribute("save-url")
    val smde = createSMDE(element)
    val informer = SimpleTextInformer.fromWrapper(wrapper)
    new Updater[SimpleMDE,String](name, smde, informer, endpoint)
  }

  private def createSMDE(element: dom.Element): Subscribable[SimpleMDE, String]  = {
    val opts = SimpleMDECreationOptions(
      Element = element,
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
