package com.hdigiorgi.showit.components

import org.scalajs.jquery.{JQuery, jQuery}
import com.hdigiorgi.showit.utils._

import scala.scalajs.js
import js.JSConverters._

class SortableList(wrapperId: String) extends Subscribable[SortableList, js.Array[String]] {
  private val wrapper = `$#`(wrapperId, "wrapped-sortable-list")
  private val informer = SimpleTextInformer.fromElement(`$#`(wrapperId,"input-indicator"))
  private val emptyElement = `$#0`(wrapperId, "wrapper-sortable-list-empty-element")
  private val itemContainer = `$#`(wrapperId, "wrapped-sortable-list-item-container")
  private var onChangeCallbacks: Seq[() => Unit] = Seq.empty
  private val endpoint = `!attr`(wrapper, "save-url")

  {
    containerChildren foreach addCallbacks
    addNewEntry()
    new Updater[SortableList, js.Array[String]](wrapperId, this, informer, endpoint)
  }

  private def containerChildren: Seq[JQuery] = {
    var r: Seq[JQuery] = Seq.empty
    val childrenOpt = `$[]opt`(itemContainer, "item")
    if(childrenOpt.isEmpty) return r
    val children = childrenOpt.get

    for(i <- 0 until children.length) {
      r = jQuery(children(i)) +: r
    }
    r.reverse
  }
  private def containerChildCount: Integer = containerChildren.length
  private def getValueHolder(entry: JQuery): JQuery = `$[]`(entry, "value-holder")
  private def getValueOfEntry(entry: JQuery): String = getValueHolder(entry).`val`().asInstanceOf[String].trim
  private def getValues(): Seq[String] = containerChildren map getValueOfEntry

  private def addCallbacks(entry: JQuery): Unit = {
    `$[]`(entry, "add-new").on("click", () => addNewEntry(after = entry))
    entry.on("input", () => onEntryInput(entry))
  }

  private def addNewEntry(after: JQuery = null): Unit = {
    if(after!=null && getValueOfEntry(after).isEmpty) return
    val clone = emptyElement.clone()
    if(after == null) {
      if(containerChildCount <= 0) clone.appendTo(itemContainer)
    } else {
      clone.insertAfter(after)
    }
    addCallbacks(clone)
  }

  private def onEntryInput(entry: JQuery): Unit = {
    val value = getValueOfEntry(entry)
    if(value.isEmpty && containerChildCount > 1) {
      entry.remove()
    }
    onValuesChange()
  }

  private def onValuesChange(): Unit = onChangeCallbacks foreach (_())

  override val getSubscribedElement: SortableList = this
  override def getSubscribedValue: js.Array[String] = getValues().toJSArray
  override def subscribeToChange(callback: () => Unit): Unit = {
    this.onChangeCallbacks = callback +: this.onChangeCallbacks
  }
}
