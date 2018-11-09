package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.components.{MDEditor, PlainTextUpdater, Uploader, SortableList}

class SiteEdit {
  def run(): Unit = {
    new SortableList("site-links")
    Uploader("site-image")
    MDEditor.create("site-description")
    PlainTextUpdater.create("site-name")
  }
}
