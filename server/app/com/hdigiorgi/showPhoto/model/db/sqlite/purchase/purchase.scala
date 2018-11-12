package com.hdigiorgi.showPhoto.model.db.sqlite.purchase

import java.time.Instant

import com.hdigiorgi.showPhoto.model.PurchasePI
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import com.hdigiorgi.showPhoto.model.purchase.Purchase
import slick.jdbc.SQLiteProfile.api._


object SQLPurchase {
  val TableName = "PURCHASES"
  type Item = String; val ItemColumnId = "ITEM"
  type Tracking = String; val TrackingColumnId = "TRACKING"
  type Ip = String; val IpColumnId = "IP"
  type PurchaseInstant = Long; val PurchaseTimeColumnId = "INSTANT"

  type Tuple = (Item, Ip, Tracking, PurchaseInstant)
}

class SQLPurchase(tag: Tag) extends Table[SQLPurchase.Tuple](tag, SQLPurchase.TableName)  {
  import SQLPurchase._
  def item = column[Item](ItemColumnId)
  def tracking = column[Tracking](TrackingColumnId)
  def ip = column[Ip](IpColumnId)
  def purchaseInstant = column[PurchaseInstant](PurchaseTimeColumnId)

  override def * = (item, tracking, ip, purchaseInstant)
}

class SQLPurchasePI extends PurchasePI{

  def readMatching(purchase: Purchase): Option[Purchase] = {
    readMatching(purchase.itemId, purchase.trackingCode, purchase.ip)
  }

  def readMatching(itemId: String, tracking: String, ip: String): Option[Purchase] = DB.runSync {
    table.filter{p =>
      p.item === itemId &&
        (p.tracking === tracking || p.ip === ip)
    }.sortBy(_.purchaseInstant.desc).take(1).result
  }.headOption.map(fromTuple)

  def insert(purchase: Purchase): Unit = DB.runSyncThrowIfNothingAffected{
    table += toTuple(purchase)
  }


  override def delete(itemId: String, trackingCode: String): Unit = DB.runSyncThrowIfNothingAffected{
    table.filter{p =>
      p.item === itemId && p.tracking === trackingCode
    }.delete
  }

  def init(): SQLPurchasePI = {
    DB.ensureTableExists(table)
    this
  }

  private def fromTuple(tuple: SQLPurchase.Tuple): Purchase = {
    Purchase(itemId = tuple._1,
      trackingCode = tuple._2,
      ip = tuple._3,
      createdAt = Instant.ofEpochMilli(tuple._4))
  }

  private def toTuple(purchase: Purchase): SQLPurchase.Tuple = {
    (purchase.itemId, purchase.trackingCode, purchase.ip, purchase.createdAt.toEpochMilli)
  }

  private val table = TableQuery[SQLPurchase]
}