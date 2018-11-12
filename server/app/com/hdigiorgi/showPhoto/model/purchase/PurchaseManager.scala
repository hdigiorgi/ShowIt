package com.hdigiorgi.showPhoto.model.purchase

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.hdigiorgi.showPhoto.model.{DBInterface, PurchasePI, StringId}
import filters.TrackingHolder
import play.api.Configuration

class PurchaseManager(val db: PurchasePI) {

  def hasValidPurchase(postId: StringId, tracking: TrackingHolder): Boolean = {
    db.readMatching(Purchase(postId, tracking)).exists{ purchase =>
      val now = Instant.now()
      val threeHoursAfter = purchase.createdAt.plus(3, ChronoUnit.HOURS)
      threeHoursAfter.isAfter(now)
    }
  }

  def createPurchase(postId: StringId,
                     tracking: TrackingHolder,
                     createdAt: Option[Instant] = None): Purchase = {
    val purchase = Purchase(postId, tracking)
    val purchaseWithCreation = createdAt.map(purchase.withCreatedAt).getOrElse(purchase)
    db.insert(purchaseWithCreation)
    purchase
  }

  def deletePurchase(postId: StringId, tracking: TrackingHolder): Unit = {
    deletePurchase(postId, tracking.userTrackingCode)
  }
  def deletePurchase(postId: StringId, tracking: String): Unit = {
    db.delete(postId, tracking)
  }

}

object PurchaseManager {
  def apply()(implicit cfg: Configuration): PurchaseManager = {
    new PurchaseManager(DBInterface.getDB().purchase)
  }
}