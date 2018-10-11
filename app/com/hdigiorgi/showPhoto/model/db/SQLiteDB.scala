package com.hdigiorgi.showPhoto.model.db
import java.io.File
import java.sql.DriverManager

import com.hdigiorgi.showPhoto.model.ExecutionContext._

import scala.concurrent.duration._
import javax.inject._
import com.hdigiorgi.showPhoto.model.{License, _}
import play.api.Configuration
import slick.jdbc.SQLiteProfile
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{Await, Future, Promise}

class SQLiteLicense(tag: Tag) extends Table[(String, String, String, Float)](tag, "license") {
  def licenseId = column[String]("LICENSE_ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def description = column[String]("DESCRIPTION")
  def price = column[Float]("PRICE")
  override def * = (licenseId, name, description, price)
}

class SQLiteLicensePI @Inject() (config: Configuration) extends LicensePI {
  val tableQuery = TableQuery[SQLiteLicense]

  override def create(element: License): License = ???

  override def update(element: License): License = ???

  override def read(key: String): License = ???

  override def delete(element: License): License = ???

  override def init(): Unit = {
    SQLite.ensureDatabaseExists(config)
    val check = SQLite.checkIfTableNotExist(SQLite.db, tableQuery, ()) {
      val create = tableQuery.schema.create
      SQLite.db.run(create).wait()
    }
    Await.result(check, Duration.Inf)
  }
}

object SQLite { self =>
  val configurationPath = "slick.dbs.default"
  val urlPath = f"${configurationPath}.url"
  var _db: SQLiteProfile.backend.Database = null

  def db() = self._db

  def ensureDatabaseExists(config: Configuration): Unit = {
    if (db == null){
      ensureSQLiteFileExists(config)
      ensureSlickProfile(config)
    }
  }

  private def ensureSlickProfile(config: Configuration): Unit = {
    self._db = Database.forConfig(configurationPath, config.underlying)
  }

  private def ensureSQLiteFileExists(config: Configuration): Unit = {
    val url: String = config.get[String](urlPath)
    val path = url.substring("jdbc:sqlite:".length)
    val file = new File(path).getParentFile
    if(!file.exists()) file.mkdirs()
    val con = DriverManager.getConnection(url)
    con.close()
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
}