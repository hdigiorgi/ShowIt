package com.hdigiorgi.showit.pages

import com.hdigiorgi.showit.components.{MDEditor, PlainTextUpdater, Uploader}

class PostEdit {
  def run(): Unit = {
    PlainTextUpdater.create("post-title")
    Uploader.create("post-image", () => println("post image complete"))
    Uploader.create("post-attachment", () => println("post attachment complete"))
    MDEditor.create("post-content")
  }
}
