package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.components.{MDEditor, PlainTextUpdater, Uploader}

class SiteEdit {
  def run(): Unit = {
    Uploader.create("site-image", () => println("site image complete"))
    MDEditor.create("site-description")
    PlainTextUpdater.create("site-title")
  }
}
