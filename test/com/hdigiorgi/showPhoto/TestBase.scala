package test
import com.hdigiorgi.showPhoto.model.{DBInterface, PostPI}
import com.hdigiorgi.showPhoto.model.DBInterface.DB
import com.hdigiorgi.showPhoto.model.post.PostManager
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSuite, Matchers, PrivateMethodTester}
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerTest}
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting

import scala.collection.JavaConverters._

trait TestBase extends FunSuite
               with GuiceOneAppPerTest with Injecting with Matchers
               with GuiceFakeApplicationFactory with PrivateMethodTester{

  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    val configStringSet =
      config.entrySet().asScala.map { entry => (entry.getKey, entry.getValue()) }
    val configMap = configStringSet.toMap + allowDatabaseDeletion
    GuiceApplicationBuilder().configure(configMap).build()
  }

  private val allowDatabaseDeletion = "unsecure.allow_db_destroy" -> "allow"
  protected implicit val configuration: Configuration = fakeApplication().configuration

  def wrapCleanDB[A](op: DBInterface => A)(implicit configuration: Configuration): A = this.synchronized {
    val db = DBInterface.getDB()
    db.init(configuration)
    db.destroy()
    db.init(configuration)
    op(db)
  }

  def wrapCleanPostDB[A](op: PostPI => A)(implicit configuration: Configuration): A = {
    wrapCleanDB(dbi => op(dbi.post))
  }

  def wrapPostManager[A](op: PostManager => A): A = wrapCleanDB{ db =>
    op(PostManager(db))
  }

  def getVal[A](obj: Object, name: Symbol): A = {
    val privateMethod = PrivateMethod[A](name)
    obj invokePrivate privateMethod()
  }

}