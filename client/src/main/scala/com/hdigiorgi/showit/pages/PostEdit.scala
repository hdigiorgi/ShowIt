package com.hdigiorgi.showit.pages

import com.hdigiorgi.showit.components.{FullScreenLoading, MDEditor, PlainTextUpdater, Uploader}

class PostEdit {
  def run(): Unit = {
    val show = FullScreenLoading.create("post-edit")
    MDEditor.create("post-content")
    PlainTextUpdater.create("post-title")
    Uploader.create("post-attachment", () => {
      Uploader.create("post-image", () => {
        show()
      })
    })
  }
}
