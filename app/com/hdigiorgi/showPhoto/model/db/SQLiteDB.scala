package com.hdigiorgi.showPhoto.model.db
import java.io.File
import java.sql.DriverManager

import com.hdigiorgi.showPhoto.model.ExecutionContext._

import scala.concurrent.duration._
import javax.inject._
import com.hdigiorgi.showPhoto.model.{License, _}
import play.api.Configuration
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.SQLiteProfile
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{Await, Future, Promise}

class SQLiteLicense(tag: Tag) extends Table[(String, Float, Boolean)](tag, "license") {
  def licenseId = column[String]("LICENSE_ID", O.PrimaryKey)
  def price = column[Float]("PRICE")
  def enabled = column[Boolean]("ENABLED")
  override def * = (licenseId, price, enabled)
}

class SQLiteLicensePI @Inject() (config: Configuration) extends LicensePI {
  val tableQuery = TableQuery[SQLiteLicense]

  override def update(element: License): Unit = {
    val insertOrUpdate = tableQuery.insertOrUpdate(toTuple(element))
    SQLite.runSync(insertOrUpdate)
  }

  override def read(key: License.Id): Option[License] = {
    val q = tableQuery.filter(_.licenseId === key.id).result
    val seq = SQLite.runSync(q)
    seq.isEmpty match {
      case true => None
      case false => Some(fromTuple(seq.head))
    }
  }

  override def delete(key: License.Id): Unit = {
    val q = tableQuery.filter(_.licenseId === key.id).delete
    SQLite.runSync(q)
  }

  private def toTuple(license: License) =
    (license.id.id, license.price.value(), license.enabled)

  private def fromTuple(tuple: (String, Float, Boolean)): License = tuple match {
    case(id, price, enabled) => License(License.Id(id), Price(price), enabled)
  }

  override def init(): Unit = {
    SQLite.ensureDatabaseExists(config)
    ensureTableExists()
    ensureDefaultLicensesExist()
  }

  private def ensureTableExists(): Unit = {
    if (!SQLite.checkIfTableNotExist(SQLite.db, tableQuery)) {
      SQLite.runSync(tableQuery.schema.create)
    }
  }

  private def ensureDefaultLicensesExist(): Unit = {
    License.getDefaultLicenses().map{default =>
      (read(default.id), default)
    }.foreach{
      case (Some(_),_) => ()
      case (None, default) => update(default)
    }
  }

}

object SQLite { self =>
  val configurationPath = "slick.dbs.default"
  val urlPath = f"${configurationPath}.url"
  private var _db: SQLiteProfile.backend.Database = null
  private var _configuration: Configuration =  null

  def db = self._db
  def config: Configuration = self._configuration
  def config_= (c : Configuration): Unit = _configuration = c

  def ensureDatabaseExists(config: Configuration): Unit = {
    if (db == null){
      _configuration = config
      ensureSQLiteFileExists(config)
      _db = getSlickProfile(config)
    }
  }

  def destroy(): Unit = {
    if (_configuration != null) {
      config.getOptional[String]("unsecure.allow_db_destroy") match {
        case Some("allow") =>
          val (_, file) = getDBFile(config)
          if (_db != null) { db.close(); _db = null }
          if (!file.delete())
            throw new RuntimeException("unable to delete database")
        case _ =>
          throw new SecurityException("the database should NOT be deleted")
      }
    }
  }


  private def getSlickProfile(config: Configuration): SQLiteProfile.backend.Database = {
    Database.forConfig(configurationPath, config.underlying)
  }

  private def getDBFile(config: Configuration): (String, File) = {
    val url: String = config.get[String](urlPath)
    val path = url.substring("jdbc:sqlite:".length)
    val file = new File(path)
    (url, file)
  }

  private def ensureSQLiteFileExists(config: Configuration): Unit = {
    val (url, file) = getDBFile(config)
    val folder = file.getParentFile
    if(!folder.exists()) folder.mkdirs()
    val con = DriverManager.getConnection(url)
    con.close()
  }

  def checkIfTableNotExist[A <: slick.lifted.AbstractTable[_] , B]
  (db: Database, table: TableQuery[A]): Boolean = {
    val existingTables = db.run(MTable.getTables)
    val doesExist = existingTables map (tables =>
      tables.exists( mtable =>
        mtable.name.name == table.baseTableRow.tableName
      ))
    Await.result(doesExist, Duration.Inf)
  }

  final def runSync[R](a: DBIOAction[R, NoStream, Nothing], duration: Duration = Duration.Inf ): R = {
    val run = db.run(a)
    Await.result(run, duration)
  }

  final def run[R](a: DBIOAction[R, NoStream, Nothing] ): Future[R] = db.run(a)

}