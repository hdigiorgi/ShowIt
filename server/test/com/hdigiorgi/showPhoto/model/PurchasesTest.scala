package com.hdigiorgi.showPhoto.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import com.hdigiorgi.showPhoto.UnitTestBase
import com.hdigiorgi.showPhoto.model.purchase.PurchaseManager
import filters.CustomTrackingHolder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PurchasesTest extends UnitTestBase {
  val m = PurchaseManager()

  test("store purchase") {
    val postId = randomId
    val tracking = CustomTrackingHolder(randomId, randomId)
    m.hasValidPurchase(postId, tracking) shouldBe PurchaseManager.ErrorMessages.NoPurchaseRegistered
    m.createPurchase(postId, tracking)
    m.hasValidPurchase(postId, tracking).isRight shouldBe true
    m.deletePurchase(postId,tracking)
    m.hasValidPurchase(postId, tracking) shouldBe PurchaseManager.ErrorMessages.NoPurchaseRegistered
  }

  test("invalid purchase renewal") {
    val postId = randomId
    val tracking = CustomTrackingHolder(randomId, randomId)
    m.createPurchase(postId, tracking, Some(Instant.now().minus(4, ChronoUnit.HOURS)))
    m.hasValidPurchase(postId, tracking) shouldBe PurchaseManager.ErrorMessages.Expired
    m.createPurchase(postId, tracking, Some(Instant.now().minus(2, ChronoUnit.HOURS)))
    m.hasValidPurchase(postId, tracking).isRight shouldBe true
    m.deletePurchase(postId,tracking)
  }




}
