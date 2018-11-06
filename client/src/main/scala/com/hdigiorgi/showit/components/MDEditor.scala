package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.external.simpleMDE.{SimpleMDE, SimpleMDECreationOptions}
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import scala.scalajs.js

object MDEditor {
  def create(wrapperId: String): Updatable[SimpleMDE] = {
    val wrapper  = jQuery(f"#$wrapperId-markdown-editor-form-group")
    val element = wrapper.children(".md-editor").get(0)
    val name = element.getAttribute("name")
    val endpoint = element.getAttribute("save-url")
    val smde = createSMDE(element)
    val informer = SimpleTextInformer.fromWrapper(wrapper)
    new Updatable[SimpleMDE](name, smde, informer, endpoint)
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
    js.Dynamic.global.smde = editor
    new Subscribable[SimpleMDE, String] {
      override val element: SimpleMDE = editor
      override def value: String = editor.value
      override def subToChange(callback: () => Unit): Unit = {
        editor.codemirror.on("change", () => callback())
      }
    }
  }

}
