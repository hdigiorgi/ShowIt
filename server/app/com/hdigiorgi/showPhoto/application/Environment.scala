package com.hdigiorgi.showPhoto.application

import com.typesafe.config.ConfigFactory
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import collection.JavaConverters._

trait Environment {

  def withValue[A](prod: A = null, dev: A = null, test: A = null): A = {
    val optProd = Some(prod).filter(_ => isProd)
    val optDev = Some(dev).filter(_ => isDev)
    val optTest = Some(test).filter(_ => isTest)

    optProd.orElse(optDev).orElse(optTest).
      orElse(Some(test)).orElse(Some(dev)).orElse(Some(prod)).get
  }

  def isTest: Boolean = false
  def isDev: Boolean = false
  def isProd: Boolean = false

  lazy val application: Application = {
    val config = ConfigFactory.load(configurationFile)
    val configMap = config.entrySet.asScala.map{entry => (entry.getKey, entry.getValue)}.toMap
    GuiceApplicationBuilder().configure(configMap).build()
  }

  lazy val configuration: Configuration = application.configuration

  val configurationFile: String
}
object Development extends Environment {
  override val configurationFile: String = "application.conf"
  override def isDev: Boolean = true
}
object Production extends Environment {
  override val configurationFile: String = "application.production.conf"
  override def isProd: Boolean = true
}
object Test extends Environment {
  override val configurationFile: String = "application.test.conf"
  override def isTest: Boolean = true
}
object Environment{

  def apply()(implicit cfg: Configuration): Environment = {
    fromString(cfg.get[String]("ENV"))
  }

  def fromString(value: String): Environment = {
    value.toLowerCase() match {
      case "d" | "dev" | "development" => Development
      case "p" | "prod" | "production" => Production
      case "t" | "test" => Test
      case _=> throw new RuntimeException(f"no environment found: $value")
    }
  }

}