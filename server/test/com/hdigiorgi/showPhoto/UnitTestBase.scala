package com.hdigiorgi.showPhoto
import java.io.File
import java.util.UUID

import com.hdigiorgi.showPhoto.model.{DBInterface, PostPI}
import com.hdigiorgi.showPhoto.model.DBInterface.DB
import com.hdigiorgi.showPhoto.model.files._
import com.hdigiorgi.showPhoto.model.post.PostManager
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.scalatest.{FunSuite, Matchers, PrivateMethodTester}
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerTest}
import org.scalawebtest.core.IntegrationFunSpec
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.test.{Injecting, TestServer}
import play.test

import scala.collection.JavaConverters._

trait UnitTestBase extends FunSuite
               with GuiceOneAppPerTest with Injecting with Matchers
               with GuiceFakeApplicationFactory with PrivateMethodTester {

  override def fakeApplication(): Application = UnitTestBase.fakeApplication()

  protected val application: Application = fakeApplication()
  protected implicit val configuration: Configuration = application.configuration
  protected implicit val controllerComponents: ControllerComponents =
    fakeApplication().injector.instanceOf(classOf[ControllerComponents])


  protected def randomId: String = UUID.randomUUID().toString

  protected def deleteFiles()(implicit configuration: Configuration): Unit = {
    val locations = Seq(
      new PostAttachmentDB().location,
      new PostImagesDB().location,
      new SiteImagesDB().location
    )
    for(location <- locations){
      val file = new File(location)
      if(file.exists()) FileUtils.deleteDirectory(file)
    }
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


object UnitTestBase {
  def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    val configStringSet =
      config.entrySet().asScala.map { entry => (entry.getKey, entry.getValue()) }
    val configMap = configStringSet.toMap + allowDatabaseDeletion
    GuiceApplicationBuilder().configure(configMap).build()
  }

  private val allowDatabaseDeletion = "unsecure.allow_db_destroy" -> "allow"
}
