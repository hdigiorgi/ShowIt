package com.hdigiorgi.showit

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main {
  def main(args: Array[String]): Unit = {
    println("Hello world! bitch")
  }

  @JSExport
  def f(): String = {
    "HI god"
  }
}
