package com.hdigiorgi.showit

import org.scalajs.jquery.JQuery
import org.scalajs.jquery.jQuery
import scalajs.js

package object utils {
  def $(id: String): JQuery = {
    val element = jQuery(id)
    if(element.length <= 0) `console error`(f"can not find $id")
    element
  }

  def `$#`(id: String): JQuery = $(f"#$id")
  def `$#`(id: String, postfix: String): JQuery = $(f"#$id-$postfix")
  def `$$`(element: JQuery, id: String): JQuery = {
    val child = element.children(id)
    if(child.length <= 0) `console error`(f"can not find child '$id' in ${element.humanReadable}")
    child
  }
  def `$$0`(element: JQuery, id: String): JQuery = {
    jQuery(`$$`(element, id).get(0))
  }

  def `!attr`(element: JQuery, name: String): String = {
    element.attr(name).toOption match {
      case None =>
        `console error`(f"${element.humanReadable} do not have attribute $name")
        null
      case Some(value) => value
    }
  }

  def `!attr#`(element: JQuery, name: String): JQuery = {
    val id = `!attr`(element, name)
    `$#`(id)
  }

  def `attr`(element: JQuery, name: String): Option[String] = element.attr(name).toOption

  def `console error`(errorMsg: js.Any): Unit = {
    js.Dynamic.global.console.error(errorMsg)
  }

  class `JQUERYPrintable`(j: JQuery) {
    def humanReadable: String = f"""'id="${j.attr("id")}" class="${j.attr("class")}"'"""
  }
  implicit def `toJQUERYPrintable`(j: JQuery): `JQUERYPrintable` = new `JQUERYPrintable`(j)

}
