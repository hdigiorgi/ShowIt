package com.hdigiorgi.showit
import org.scalajs.jquery._
import org.scalajs.dom
import dom.document

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main extends {

  @JSExport
  def main(): Unit = jQuery{
    jQuery("#APP").attr("run").map{
      case "landing" => landingPage()
    }
  }

  def landingPage(): Unit = {
    jQuery(dom.window).on("load", (_: JQueryEventObject) => {
      println("on")
    })
  }


}

