package com.hdigiorgi.showPhoto.model.db.sqlite

import java.io.File
import java.sql.DriverManager
import com.hdigiorgi.showPhoto.model.ExecutionContext._
import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.meta.SQLMetaPI
import com.hdigiorgi.showPhoto.model.db.sqlite.post.SQLPostPI
import com.hdigiorgi.showPhoto.model.db.sqlite.purchase.SQLPurchasePI
import com.hdigiorgi.showPhoto.model.db.sqlite.site.SQLSitePI
import com.hdigiorgi.showPhoto.model.db.sqlite.user.SQLUserPI
import org.sqlite.{SQLiteErrorCode, SQLiteException}
import play.api.Configuration
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._
import slick.jdbc.meta.MTable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object DB extends DBInterface { self =>
  val configurationPath = "slick.dbs.default"
  val urlPath = f"$configurationPath.url"
  private var _db: SQLiteProfile.backend.Database = _
  private var _configuration: Configuration =  _
  private var _meta_db: SQLMetaPI = _
  private var _user_db: SQLUserPI = _
  private var _post_db: SQLPostPI = _
  private var _site_db: SQLSitePI = _
  private var _purchase_db: SQLPurchasePI = _
  private var _initialized = false


  override def post: PostPI = _post_db

  override def site: SitePI = _site_db

  override def purchase: PurchasePI = _purchase_db

  override def meta: MetaPI = _meta_db

  override def user: UserPI = _user_db

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
    destroy_PutVarsInNull()
  }

  private def destroy_PutVarsInNull(): Unit = {
    _db = null
    _configuration = null
    _meta_db = null
    _user_db = null
    _post_db = null
    _site_db = null
    _purchase_db =  null
    _initialized = false
  }

  def db = self._db

  private def ensureDatabaseExists(): Unit = {
    if (db == null){
      ensureSQLiteFileExists()
      _db = getSlickProfile()
      _meta_db = new SQLMetaPI().init()
      _user_db = new SQLUserPI().init()
      _post_db = new SQLPostPI().init()
      _site_db = new SQLSitePI().init()
      _purchase_db = new SQLPurchasePI().init()
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
    Class.forName("org.sqlite.JDBC");
    val con = DriverManager.getConnection(url)
    con.close()
  }

  def ensureTableExists[A <: Table[_]](table: TableQuery[A]): Boolean = {
    if(!checkIfTableNotExist(table)) {
      runSync(table.schema.create)
      true
    } else {
      false
    }
  }

  def checkIfTableNotExist[A <: slick.lifted.AbstractTable[_] , B]
  (table: TableQuery[A]): Boolean = {
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

  def runSyncThrowIfNothingAffected(a: DBIOAction[Int, NoStream, Nothing], duration: Duration = Duration.Inf ): Int = {
    val r: Int = runSync(a, duration)
    if(r <= 0) throw new SQLiteException("Query didn't change anything", SQLiteErrorCode.getErrorCode(-1))
    r
  }

  def run[R](a: DBIOAction[R, NoStream, Nothing] ): Future[R] = db.run(a)

}