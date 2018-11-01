package com.hdigiorgi.showPhoto.model.db.sqlite.meta

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import slick.jdbc.SQLiteProfile.api._

object SQLiteMetaType {
  type Tuple = (String, String)
}

class SQLiteMeta(tag: Tag) extends Table[SQLiteMetaType.Tuple](tag, "META") {
  def entry= column[String]("ENTRY", O.PrimaryKey)
  def value= column[String]("VALUE")
  override def * = (entry, value)
}

class SQLiteMetaPI() extends MetaPI {self =>
  val table = TableQuery[SQLiteMeta]


  override def insert(element: Meta): Unit = ???

  override def update(element: Meta): Unit = {
    val insertOrUpdate = table.insertOrUpdate(toTuple(element))
    DB.runSync(insertOrUpdate)
  }

  override def read(key: String): Option[Meta] = {
    val q = table.filter(_.entry === key).result
    val seq = DB.runSync(q)
    seq.headOption.map(fromTuple)
  }

  override def delete(key: String): Unit = {
    val delete = table.filter(_.entry === key).delete
    DB.runSync(delete)
  }

  def init(): SQLiteMetaPI = {
    DB.ensureTableExists(table)
    self
  }

  private def fromTuple(tuple: SQLiteMetaType.Tuple): Meta = tuple match {
    case (key, value) => Meta(key, value)
  }

  private def toTuple(meta: Meta): SQLiteMetaType.Tuple = (meta.entry, meta.value)

}

