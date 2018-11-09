package com.hdigiorgi.showit

import org.scalajs.jquery.JQuery
import org.scalajs.jquery.jQuery
import scala.scalajs.js.Any
import scalajs.js
import js.JSConverters._

package object utils {
  def $(id: String): JQuery = {
    val element = jQuery(id)
    if(element.length <= 0) `console error`(f"can not find $id")
    element
  }

  def $opt(id: String): Option[JQuery] = {
    if(id.trim.equals("")) return None
    val element = $(id)
    if(element.length <= 0) None else Some(element)
  }

  def `$#`(id: String): JQuery = $(f"#$id")
  def `$#`(id: String, postfix: String): JQuery = $(f"#$id-$postfix")
  def `$#0`(id: String, postfix: String): JQuery = {
    val element = `$#`(id, postfix)
    val children = element.children()
    if(children.length <= 0)  `console error`(s"no child found in ${element.humanReadable}")
    jQuery(children(0))
  }
  def `$$`(element: JQuery, id: String): JQuery = {
    val child = element.children(id)
    if(child.length <= 0) `console error`(f"can not find child '$id' in ${element.humanReadable}")
    child
  }
  def `$[]opt`(element: JQuery, attribute: String): Option[JQuery] = {
    val child = element.find(f"[$attribute]")
    if(child.length <= 0) None else Some(child)
  }
  def `$[]`(element: JQuery, attribute: String): JQuery = {
    `$[]opt`(element, attribute) match{
      case None =>
        `console error`(f"can not find child by attribute $attribute in ${element.humanReadable}")
        null
      case Some(child) =>
        child
    }
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

  def ?[A](any: A): js.Dynamic = {
    any.asInstanceOf[js.Dynamic]
  }

  def `{`(fields: (String, Any)*): js.Any = {
    fields.toMap.toJSDictionary
  }
  def `[`(fields: Any*): js.Any = {
    fields.toJSArray
  }

  class `JQUERYPrintable`(j: JQuery) {
    def humanReadable: String = f"""'id="${j.attr("id")}" class="${j.attr("class")}"'"""
  }
  implicit def `toJQUERYPrintable`(j: JQuery): `JQUERYPrintable` = new `JQUERYPrintable`(j)

}
