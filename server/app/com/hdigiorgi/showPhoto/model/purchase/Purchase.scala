package com.hdigiorgi.showPhoto.model.purchase

import java.time.Instant

import com.hdigiorgi.showPhoto.model.StringId
import filters.TrackingHolder

case class Purchase(itemId: StringId,
                    trackingCode: String,
                    ip: String,
                    createdAt: Instant = Instant.now()) {
  def withCreatedAt(t: Instant): Purchase = {
    this.copy(createdAt = t)
  }
}

object Purchase {
  def apply(itemId: String, tracking: TrackingHolder): Purchase = {
    Purchase(itemId = itemId,
      trackingCode = tracking.userTrackingCode,
      ip = tracking.userIp)
  }
}