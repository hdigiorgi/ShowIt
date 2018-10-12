package com.hdigiorgi.showPhoto.model.db.sqlite.license
import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import slick.jdbc.SQLiteProfile.api._

class SQLiteLicense(tag: Tag) extends Table[(Int, Float, Boolean)](tag, "LICENSE") {
  def grade = column[Int]("grade", O.PrimaryKey)
  def price = column[Float]("PRICE")
  def enabled = column[Boolean]("ENABLED")
  override def * = (grade, price, enabled)
}

class SQLiteLicensePI() extends LicensePI { self =>
  val tableQuery = TableQuery[SQLiteLicense]

  override def update(element: License): Unit = {
    val insertOrUpdate = tableQuery.insertOrUpdate(toTuple(element))
    DB.runSync(insertOrUpdate)
  }

  override def read(key: Grade): Option[License] = {
    val q = tableQuery.filter(_.grade === key.int).result
    val seq = DB.runSync(q)
    seq.headOption.map(fromTuple)
  }

  override def delete(key: Grade): Unit = {
    val q = tableQuery.filter(_.grade === key.int).delete
    DB.runSync(q)
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
    if (!DB.checkIfTableNotExist(tableQuery)) {
      DB.runSync(tableQuery.schema.create)
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