package com.hdigiorgi.showPhoto.model.db.sqlite.user

import com.hdigiorgi.showPhoto.model._
import com.hdigiorgi.showPhoto.model.db.sqlite.DB
import com.hdigiorgi.showPhoto.model.site.Email
import slick.jdbc.SQLiteProfile.api._

object SQLUserType {
  type Tuple = (String, String, String)
}

class SQLUser(tag: Tag) extends Table[SQLUserType.Tuple](tag, "USER") {
  def id = column[String]("ID", O.PrimaryKey)
  def email = column[String]("EMAIL")
  def password = column[String]("PASSWORD")
  def emailIdx = index("email_idx", email, unique = true)
  override def * = (id, email, password)
}

class SQLUserPI() extends UserPI { self =>
  private val table = TableQuery[SQLUser]


  override def insert(element: User): Unit = DB.runSyncThrowIfNothingAffected{
    table += toTuple(element)
  }

  override def update(element: User): Unit = DB.runSyncThrowIfNothingAffected {
    table.update(toTuple(element))
  }

  override def read(key: StringId): Option[User] = {
    val q = table.filter(_.id === key.value).result
    val seq = DB.runSync(q)
    seq.headOption.map(fromTuple)
  }

  override def readByEmail(email: String): Option[User] = {
    val q = table.filter(_.email === email).result
    val seq = DB.runSync(q)
    seq.headOption.map(fromTuple)
  }

  override def delete(key: StringId): Unit = {
    val delete = table.filter(_.id === key.value).delete
    DB.runSyncThrowIfNothingAffected(delete)
  }

  def init(): SQLUserPI = {
    DB.ensureTableExists(table)
    ensureUserExist()
    self
  }

  private def toTuple(user: User): SQLUserType.Tuple =
    (user.id.value, user.email.string, user.password.value)

  private def fromTuple(tuple: SQLUserType.Tuple): User = tuple match {
    case(id, email, password) =>
      User(StringId(id), Email(email), Password.fromEncrypted(password))
  }

  private def ensureUserExist(): Unit = {
    User.defaultUsers.map{ defaultUser =>
      (readByEmail(defaultUser.email.string), defaultUser)
    } foreach {
      case (Some(_),_) => ()
      case (None, defaultUser) =>
        insert(defaultUser)
    }
  }

}
