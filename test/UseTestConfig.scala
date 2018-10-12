package test
import com.typesafe.config.ConfigFactory
import org.scalatestplus.play.guice.GuiceFakeApplicationFactory
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import scala.collection.JavaConverters._

trait UseTestConfig extends GuiceFakeApplicationFactory {
  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    val configStringSet =
      config.entrySet().asScala.map { entry => (entry.getKey, entry.getValue()) }
    val configMap = configStringSet.toMap + allowDatabaseDeletion
    GuiceApplicationBuilder().configure(configMap).build()
  }

  private val allowDatabaseDeletion = "unsecure.allow_db_destroy" -> "allow"
}
