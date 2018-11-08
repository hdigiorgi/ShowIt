package com.hdigiorgi.showit.components

import org.scalajs.jquery.JQuery
import com.hdigiorgi.showit.utils._

object PlainTextUpdater {
  def create(wrapperId: String): Updater[JQuery, String] = {
    val wrapper  = `$#`(wrapperId,"plain-text-input-form-group")
    val element = `$$0`(wrapper, ".plain-text")
    val name = `!attr`(element, "name")
    val endpoint = `!attr`(element, "save-url")
    val informer = SimpleTextInformer.fromElement(`$#`(wrapperId,"input-indicator"))
    val sub = createSubscribable(element)
    new Updater[JQuery, String](name, sub, informer, endpoint)
  }

  private def createSubscribable(element: JQuery): Subscribable[JQuery, String] = {
    new Subscribable[JQuery,String] {
      override val getSubscribedElement: JQuery = element
      override def getSubscribedValue: String = element.`val`().asInstanceOf[String]
      override def subscribeToChange(callback: () => Unit): Unit = {
        element.on("input", () => callback())
      }
    }
  }

}
