package com.hdigiorgi.showPhoto.model.db

import java.io.File
import java.net.{URI, URL}
import java.sql.DriverManager

//import com.hdigiorgi.showPhoto.model.ExecutionContext._

import scala.concurrent.duration._
import javax.inject._
import com.hdigiorgi.showPhoto.model.Persistent
import play.api.Configuration
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

class License(tag: Tag) extends Table[(String, String, String, Float)](tag, "license") {
  def licenseId = column[String]("LICENSE_ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def description = column[String]("DESCRIPTION")
  def price = column[Float]("PRICE")
  override def * = (licenseId, name, description, price)
}

class PersistentLicense @Inject() (config: Configuration) extends Persistent.License {
  val tableQuery = TableQuery[License]
  val db = Database.forConfig("slick.dbs.default")
  ensureDatabaseExists()

  override def create(element: Persistent.License): Persistent.License = ???

  override def update(element: Persistent.License): Persistent.License = ???

  override def read(key: String): Persistent.License = ???

  override def delete(element: Persistent.License): Persistent.License = ???

  override def init(): Unit = {
    val check = checkIfTableNotExist(db, tableQuery, ()) {
      val create = tableQuery.schema.create
      db.run(create).wait()
    }
    Await.result(check, Duration.Inf)
  }

  def checkIfTableNotExist[A <: slick.lifted.AbstractTable[_] , B]
    (db: Database, table: TableQuery[A],default: B)
    (ifNotExist: => B): Future[B] = {
    val existingTables = db.run(MTable.getTables)
    existingTables.flatMap( tables =>
      tables.contains(table) match {
        case false => Promise.successful(default).future
        case true => Future(ifNotExist)
      }
    )
  }

  def ensureDatabaseExists(): Unit = {
    val url: String = config.get[String]("slick.dbs.default.url")
    val path = url.substring("jdbc:sqlite:".length)
    val file = new File(path).getParentFile
    if(!file.exists()) file.mkdirs()
    val con = DriverManager.getConnection(url)
    con.close()
  }
}

class Item(tag: Tag) extends Table[(String, String, Float, Boolean, Int, Int, Long)](tag, "item") {
  def itemId: Rep[String] = column[String]("ITEM_ID", O.PrimaryKey)
  def description: Rep[String] = column[String]("DESCRIPTION")
  def price: Rep[Float] = column[Float]("PRICE")
  def onSale: Rep[Boolean] = column[Boolean]("ON_SALE")
  def stock: Rep[Int] = column[Int]("STOCK")
  def imageCount: Rep[Int] = column[Int]("IMAGE_COUNT")
  def createdAt: Rep[Long] = column[Long]("CREATED_AT")
  override def * = (itemId, description, price, onSale, stock, imageCount, createdAt)
}

object Item {
  val items = TableQuery[Item]
}

class Purchase(tag: Tag) extends Table[(String, String, String, String, Float, Long, Long)](tag, "purchase") {
  def purchaseId = column[String]("PURCHASE_ID", O.PrimaryKey)
  def itemId = column[String]("ITEM_ID")
  def itemFk = foreignKey("ITEM_FK", itemId, Item.items)(_.itemId)
  def email = column[String]("EMAIL")
  def status = column[String]("STATUS")
  def price = column[Float]("PRICE")
  def startedAt = column[Long]("STARTED_AT")
  def endedAt = column[Long]("ENDED_AT")

  override def * = (purchaseId, itemId, email, status, price, startedAt, endedAt)
}

object Purchase {
  val purchases = TableQuery[Item]
}
