package com.hdigiorgi.showit.components

import org.scalajs.jquery.{JQuery, jQuery}

object FullScreenLoading {
  def create(wrapperId: String): () => Unit = {
    val showWhenLoading = jQuery(f"#$wrapperId-loading-wrapper-doing-loading")
    val showWhenReady = jQuery(f"#$wrapperId-loading-wrapper-ended-loading")
    showWhenLoading.css("opacity", 1)
    createShowCallback(showWhenLoading, showWhenReady)
  }

  private def createShowCallback(showWhenLoading: JQuery,
                                 showWhenReady: JQuery): () => Unit = () => {
    showWhenLoading.css("opacity", 0)
    showWhenReady.css("opacity", 1)
  }
}
