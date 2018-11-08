package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.utils._
import org.scalajs.dom
import org.scalajs.jquery.JQuery
import org.scalajs.jquery.jQuery

class AdminIndex {
  private val speed = 200
  private val defaultMenu = `$#`("main-menu")
  private val menuContainer = `$#`("menu-container")
  jQuery(dom.window).bind("hashchange",() => onHashChange())
  onHashChange()

  private def show(element: JQuery): Unit = {
    if(!element.is(":hidden")) return
    val children = menuContainer.children()
    for(i <- 0 until children.length){
      val child = jQuery(children(i))
      if(child.is(":visible")){
        slideLeftOut(child)
      }
    }
    slideRightIn(element)
  }

  private def onHashChange(): Unit = {
    onHashChange(dom.window.top.location.hash)
  }

  private def onHashChange(hash: String): Unit = {
    $opt(hash) match {
      case None => show(defaultMenu)
      case Some(element) => show(element)
    }
  }

  private def slideLeftOut(element: JQuery): Unit = {
    ?(element).velocity(`{`(
      "properties" -> `{`("opacity" -> 0.1, "translateX" -> "-100%", "translateZ" -> 0 ),
      "options" -> `{`("duration" -> speed, "display" -> "none")
    ))
  }

  private def slideRightIn(element: JQuery): Unit = {
    ?(element).velocity(`{`(
      "properties" -> `{`("opacity" -> 1, "translateX" -> `[`(0, "100%") , "translateZ" -> 0 ),
      "options" -> `{`("duration" -> speed, "display" -> "block", "delay" -> speed )
    ))
  }

}
