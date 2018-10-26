package test
import java.io.File

import com.hdigiorgi.showPhoto.model.{DBInterface, PostPI}
import com.hdigiorgi.showPhoto.model.DBInterface.DB
import com.hdigiorgi.showPhoto.model.files.{AttachmentFileDB, ImageFileDB}
import com.hdigiorgi.showPhoto.model.post.PostManager
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
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

  protected def deleteFiles()(implicit configuration: Configuration): Unit = {
    val location1 = new File(new AttachmentFileDB().location)
    val location2 = new File(new ImageFileDB().location)
    if(location1.exists()) FileUtils.deleteDirectory(location1)
    if(location2.exists()) FileUtils.deleteDirectory(location2)
  }

  protected def wrapCleanDB[A](op: DBInterface => A)(implicit configuration: Configuration): A = this.synchronized {
    val db = DBInterface.getDB()
    db.init(configuration)
    db.destroy()
    deleteFiles()
    db.init(configuration)
    op(db)
  }

  protected def wrapCleanPostDB[A](op: PostPI => A)(implicit configuration: Configuration): A = {
    wrapCleanDB(dbi => op(dbi.post))
  }

  protected  def wrapPostManager[A](op: PostManager => A): A = wrapCleanDB{ db =>
    op(PostManager(db))
  }

}
