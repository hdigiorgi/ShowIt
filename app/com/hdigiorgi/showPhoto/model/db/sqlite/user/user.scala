package com.hdigiorgi.showPhoto.model.db.sqlite.user

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import slick.jdbc.SQLiteProfile.api._

object SQLiteUserType {
  type Tuple = (Int, String, String, Int)
}

class SQLiteUser(tag: Tag) extends Table[SQLiteUserType.Tuple](tag, "USER") {
  def id = column[Int]("ID", O.PrimaryKey)
  def email = column[String]("EMAIL")
  def password = column[String]("PASSWORD")
  def role = column[Int]("ROLE")
  override def * = (id, email, password, role)
}

class SQLiteUserPI() extends UserPI { self =>
  private val table = TableQuery[SQLiteUser]

  override def update(element: User): Unit = {
    val insertOrUpdate = table.insertOrUpdate(toTuple(element))
    DB.runSync(insertOrUpdate)
  }

  override def read(key: IntId): Option[User] = {
    val q = table.filter(_.id === key.value).result
    val seq = DB.runSync(q)
    seq.headOption.map(fromTuple)
  }

  override def delete(key: IntId): Unit = {
    val delete = table.filter(_.id === key.value).delete
    DB.runSync(delete)
  }

  def init(): SQLiteUserPI = {
    DB.ensureTableExists(table)
    ensureUserExist()
    self
  }

  private def toTuple(user: User): SQLiteUserType.Tuple =
    (user.id.value, user.email.value, user.password.value, user.role.id.value)

  private def fromTuple(tuple: SQLiteUserType.Tuple): User = tuple match {
    case(id, email, password, role) =>
      User(IntId(id), Email(email), Password.fromEncrypted(password), Role(IntId(role)))
  }

  private def ensureUserExist(): Unit = {
    User.defaultUsers.map{ defaultUser =>
      (read(defaultUser.id), defaultUser)
    } foreach {
      case (Some(_),_) => ()
      case (None, defaultUser) =>
        update(defaultUser)
    }
  }

}
