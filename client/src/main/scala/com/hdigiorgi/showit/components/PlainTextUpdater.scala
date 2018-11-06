package com.hdigiorgi.showit.components

import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery}

object PlainTextUpdater {
  def create(wrapperId: String): Updatable[JQuery] = {
    val wrapper  = jQuery(f"#$wrapperId-plain-text-input-form-group")
    val element = wrapper.children(".plain-text").get(0)
    val name = element.getAttribute("name")
    val endpoint = element.getAttribute("save-url")
    val informer = SimpleTextInformer.fromWrapper(wrapper)
    val sub = createSubscribable(element)
    new Updatable[JQuery](name, sub, informer, endpoint)
  }

  private def createSubscribable(element: dom.Element): Subscribable[JQuery, String] = {
    val jelement = jQuery(element)
    new Subscribable[JQuery,String] {
      override val element: JQuery = jelement
      override def value: String = jelement.`val`().asInstanceOf[String]
      override def subToChange(callback: () => Unit): Unit = {
        jelement.on("input", () => callback())
      }
    }
  }

}
