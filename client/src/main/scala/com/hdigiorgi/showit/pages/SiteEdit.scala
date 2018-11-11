package com.hdigiorgi.showit.pages
import com.hdigiorgi.showit.components._

class SiteEdit {
  def run(): Unit = {
    new Confirmable("site-paypal-email")
    new SortableList("site-links")
    Uploader("site-image")
    MDEditor.create("site-description")
    PlainTextUpdater.create("site-name")
  }
}
