package com.hdigiorgi.showit
import org.scalajs.jquery._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main extends {

  @JSExport
  def main(): Unit = jQuery{
    jQuery("#APP").attr("run").map{
      case "landing" => new pages.Landing().run()
      case "post" => new pages.Post().run()
      case "siteEdit" => new pages.SiteEdit().run()
      case "postEdit" => new pages.PostEdit().run()
      case "adminIndex" => new pages.AdminIndex()
      case "download" => new pages.Download()
    }
    ()
  }



}

