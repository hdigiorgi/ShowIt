package com.hdigiorgi.showPhoto.model.db
import java.io.File
import java.sql.DriverManager
import com.hdigiorgi.showPhoto.model.ExecutionContext._
import scala.concurrent.duration._
import com.hdigiorgi.showPhoto.model._
import play.api.Configuration
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.SQLiteProfile
import slick.jdbc.meta.MTable
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.{Await, Future}

class SQLiteLicense(tag: Tag) extends Table[(Int, Float, Boolean)](tag, "license") {
  def grade = column[Int]("grade", O.PrimaryKey)
  def price = column[Float]("PRICE")
  def enabled = column[Boolean]("ENABLED")
  override def * = (grade, price, enabled)
}

class SQLiteLicensePI() extends LicensePI { self =>
  val tableQuery = TableQuery[SQLiteLicense]

  override def update(element: License): Unit = {
    val insertOrUpdate = tableQuery.insertOrUpdate(toTuple(element))
    SQLite.runSync(insertOrUpdate)
  }

  override def read(key: Grade): Option[License] = {
    val q = tableQuery.filter(_.grade === key.int).result
    val seq = SQLite.runSync(q)
    seq.isEmpty match {
      case true => None
      case false => Some(fromTuple(seq.head))
    }
  }

  override def delete(key: Grade): Unit = {
    val q = tableQuery.filter(_.grade === key.int).delete
    SQLite.runSync(q)
  }

  private def toTuple(license: License) =
    (license.grade.int, license.price.value, license.enabled.boolean)

  private def fromTuple(tuple: (Int, Float, Boolean)): License = tuple match {
    case(id, price, toggle) => License(Grade(id), Price(price), Toggle(toggle))
  }

  def init(): SQLiteLicensePI = {
    ensureTableExists()
    ensureDefaultLicensesExist()
    self
  }

  private def ensureTableExists(): Unit = {
    if (!SQLite.checkIfTableNotExist(SQLite.db, tableQuery)) {
      SQLite.runSync(tableQuery.schema.create)
    }
  }

  private def ensureDefaultLicensesExist(): Unit = {
    License.defaultLicenses.map{default =>
      (read(default.grade), default)
    }.foreach{
      case (Some(_),_) => ()
      case (None, default) => update(default)
    }
  }

}

object SQLite extends DBInterface { self =>
  val configurationPath = "slick.dbs.default"
  val urlPath = f"$configurationPath.url"
  private var _db: SQLiteProfile.backend.Database = _
  private var _configuration: Configuration =  _
  private var _license_db: SQLiteLicensePI = _
  private var _initialized = false

  override def license: LicensePI = _license_db

  override def item: ItemPI = ???

  override def site: SitePI = ???

  override def purchase: PurchasePI = ???

  override def meta: FileMetaPI = ???

  override def init(configuration: Configuration): Unit = {
    if(!_initialized) {
      if(_configuration == null) _configuration = configuration
      ensureDatabaseExists()
    }
    _initialized = true
  }

  override def configuration: Configuration = _configuration

  def destroy(): Unit = self.wrapDestroy {
    val (_, file) = getDBFile(self.configuration)
    if (_db != null) db.close()
    if (!file.delete())
      throw new RuntimeException("unable to delete database")

    _db = null
    _configuration = null
    _license_db = null
    _initialized = false
  }

  def db = self._db

  private def ensureDatabaseExists(): Unit = {
    if (db == null){
      ensureSQLiteFileExists()
      _db = getSlickProfile()
      _license_db = new SQLiteLicensePI().init()
    }
  }

  private def getSlickProfile(): SQLiteProfile.backend.Database = {
    Database.forConfig(configurationPath, self.configuration.underlying)
  }

  private def getDBFile(config: Configuration): (String, File) = {
    val url: String = config.get[String](urlPath)
    val path = url.substring("jdbc:sqlite:".length)
    val file = new File(path)
    (url, file)
  }

  private def ensureSQLiteFileExists(): Unit = {
    val (url, file) = getDBFile(self.configuration)
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

  def runSync[R](a: DBIOAction[R, NoStream, Nothing], duration: Duration = Duration.Inf ): R = {
    val run = db.run(a)
    Await.result(run, duration)
  }

  def run[R](a: DBIOAction[R, NoStream, Nothing] ): Future[R] = db.run(a)

}