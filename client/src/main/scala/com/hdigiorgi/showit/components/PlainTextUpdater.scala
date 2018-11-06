package com.hdigiorgi.showit.components

import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery}

object PlainTextUpdater {
  def create(wrapperId: String): Updater[JQuery, String] = {
    val wrapper  = jQuery(f"#$wrapperId-plain-text-input-form-group")
    val element = wrapper.children(".plain-text").get(0)
    val name = element.getAttribute("name")
    val endpoint = element.getAttribute("save-url")
    val informer = SimpleTextInformer.fromWrapper(wrapper)
    val sub = createSubscribable(element)
    new Updater[JQuery, String](name, sub, informer, endpoint)
  }

  private def createSubscribable(element: dom.Element): Subscribable[JQuery, String] = {
    val jelement = jQuery(element)
    new Subscribable[JQuery,String] {
      override val getSubscribedElement: JQuery = jelement
      override def getSubscribedValue: String = jelement.`val`().asInstanceOf[String]
      override def subscribeToChange(callback: () => Unit): Unit = {
        jelement.on("input", () => callback())
      }
    }
  }

}
