package com.hdigiorgi.showit.components

import com.hdigiorgi.showit.utils._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class Confirmable(wrapperId: String) extends Subscribable[Confirmable, String]{
  private val wrapper = `$#`(wrapperId, "confirmable-wrapper")
  private val endpoint = `!attr`(wrapper, "save")
  private val input = `$#`(wrapperId, "confirmable-input")
  private val button = `$#`(wrapperId, "confirmable-button")
  private var onChangeCallbacks: Seq[() => Unit] = Seq.empty
  private val informerWrapper = `$#`(wrapperId, "confirmable-input-indicator")
  private val informer = new ConfirmableInformer
  private val updater = new Updater(wrapperId, this, informer, endpoint)
  private var previousValueBeforeSave = inputValue()
  disableSaveButton()
  input.on("keyup", () => onInputChange())

  private class ConfirmableInformer() extends SimpleTextInformer(wrapperId, informerWrapper) {
    override def informError(msg: Option[String]): Unit = {
      super.informError(msg)
      enableInput()
      enableSaveButton()
    }

    override def informWorking(_msg: Option[String]): Unit = {
      super.informWorking(_msg)
      disableInput()
      disableSaveButton()
    }

    override def informSuccess(msg: Option[String]): Unit = {
      super.informSuccess(msg)
      previousValueBeforeSave = inputValue()
      enableInput()
      disableSaveButton()
    }

  }

  private def inputValue(): String = input.`val`().asInstanceOf[String]

  private def disableSaveButton(): Unit = {
    button.removeClass("btn-outline-primary")
    button.addClass("btn-outline-secondary")
    button.attr("disabled", true)
    button.removeAttr("enabled")
    button.click(null)
  }

  private def enableSaveButton(): Unit = {
    button.addClass("btn-outline-primary")
    button.removeClass("btn-outline-secondary")
    button.removeAttr("disabled")
    button.attr("enabled", true)
    button.click(() => onSaveClicked())
  }

  private def disableInput(): Unit = {
    input.attr("disabled", true)
  }

  private def enableInput(): Unit = {
    input.removeAttr("disabled")
  }

  private def onInputChange(): Unit = {
    val currentValue = inputValue()
    informer.informHide()
    if(currentValue.equals(previousValueBeforeSave)){
      updater.cancelUpdate()
      disableSaveButton()
    }else {
      enableSaveButton()
    }
  }

  private def onSaveClicked(): Unit = {
    this.onChangeCallbacks foreach (_())
  }

  override val getSubscribedElement: Confirmable = this

  override def getSubscribedValue: String = inputValue()

  override def subscribeToChange(callback: () => Unit): Unit = this.onChangeCallbacks = callback +: this.onChangeCallbacks
}
