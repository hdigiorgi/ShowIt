package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.components.{MDEditor, Uploader}

class Site {
  def run(): Unit = {
    Uploader.createUploader("site-image", () => println("site image complete"))
    MDEditor.create("site-description")
  }
}
