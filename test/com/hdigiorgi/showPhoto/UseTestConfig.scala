package test
import com.hdigiorgi.showPhoto.model.{DBInterface, PostPI}
import com.hdigiorgi.showPhoto.model.DBInterface.DB
import com.typesafe.config.ConfigFactory
import org.scalatestplus.play.guice.GuiceFakeApplicationFactory
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.collection.JavaConverters._

trait UseTestConfig extends GuiceFakeApplicationFactory {
  import com.hdigiorgi.showPhoto.model.StringId._

  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    val configStringSet =
      config.entrySet().asScala.map { entry => (entry.getKey, entry.getValue()) }
    val configMap = configStringSet.toMap + allowDatabaseDeletion
    GuiceApplicationBuilder().configure(configMap).build()
  }

  private val allowDatabaseDeletion = "unsecure.allow_db_destroy" -> "allow"
  protected implicit val configuration: Configuration = fakeApplication().configuration

  def wrapCleanDB[A](op: DBInterface => A)(implicit configuration: Configuration): A = {
    val db = DBInterface.getDB()
    db.init(configuration)
    db.destroy()
    db.init(configuration)
    op(db)
  }

  def wrapCleanPostDB[A](op: PostPI => A)(implicit configuration: Configuration): A = {
    wrapCleanDB(dbi => op(dbi.post))
  }

}
