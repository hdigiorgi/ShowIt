package com.hdigiorgi.showit.pages

import com.hdigiorgi.showit.components._

class PostEdit {
  def run(): Unit = {
    val show = FullScreenLoading.create("post-edit")
    new Confirmable("post-price")
    MDEditor.create("post-content")
    Toggler("post-publication-status")
    PlainTextUpdater.create("post-title")
    Uploader("post-attachment", () => {
      Uploader("post-image", () => {
        show()
      })
    })
  }
}
