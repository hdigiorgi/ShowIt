package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.components.{MDEditor, PlainTextUpdater, Uploader}

class SiteEdit {
  def run(): Unit = {
    Uploader("site-image")
    MDEditor.create("site-description")
    PlainTextUpdater.create("site-title")
  }
}
