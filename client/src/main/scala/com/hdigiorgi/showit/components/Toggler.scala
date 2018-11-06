package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.utils._
import org.scalajs.jquery.jQuery

import scala.collection.mutable

case class Toggler(wrapperId: String)
    extends ResultInformer with Subscribable[Toggler, Boolean] {
  private val wrapper = `$#`(wrapperId,"wrap-toggle")
  private val toggleEndpoint = `!attr`(wrapper, "toggle")
  private var isOn = `!attr`(wrapper, "isOn").equals("true")
  private val toggleOnWrapper = `$#`(wrapperId, "wrap-toggle-on")
  private val toggleOnButton = `$$`(toggleOnWrapper, "button")
  private val toggleOffWrapper = `$#`(wrapperId, "wrap-toggle-off")
  private val toggleOffButton = `$$`(toggleOffWrapper, "button")
  private val toggleWorkingWrapper = `$#`(wrapperId, "wrap-toggle-working")

  private val errorInformerParent = `!attr#`(wrapper, "errorInformer")
  private val errorInformer = SimpleTextInformer.fromElement(errorInformerParent)

  private val callbacks: mutable.ListBuffer[()=>Unit] = mutable.ListBuffer.empty

  private val updater = new Updater[Toggler, Boolean](wrapperId, this, this, toggleEndpoint)

  this.show(isOn)

  private def show(on: Boolean): Unit = {
    toggleWorkingWrapper.hide()
    if(on){
      toggleOffWrapper.show()
      toggleOnWrapper.hide()
    }else{
      toggleOffWrapper.hide()
      toggleOnWrapper.show()
    }
  }

  toggleOnButton.click(() => startToggle())
  toggleOffButton.click(() => startToggle())
  private def startToggle(): Unit = {
    callbacks foreach { _() }
  }

  override def informError(msg: Option[String]): Unit = {
    errorInformer.informError(msg)
    this.show(isOn)
  }

  override def informWorking(msg: Option[String]): Unit = {
    errorInformer.informHide()
    toggleOffWrapper.hide()
    toggleOnWrapper.hide()
    toggleWorkingWrapper.show()
  }

  override def informSuccess(msg: Option[String]): Unit = {
    errorInformer.informHide()
    isOn = !isOn
    this.show(isOn)
  }

  override val getSubscribedElement: Toggler = this

  override def getSubscribedValue: Boolean = !isOn

  override def subscribeToChange(callback: () => Unit): Unit = {
    this.callbacks += callback
  }
}