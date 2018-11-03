package com.hdigiorgi.showPhoto.application

import com.typesafe.config.ConfigFactory
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import collection.JavaConverters._

trait Environment {
  val configurationFile: String
  lazy val application: Application = {
    val config = ConfigFactory.load(configurationFile)
    val configMap = config.entrySet.asScala.map{entry => (entry.getKey, entry.getValue)}.toMap
    GuiceApplicationBuilder().configure(configMap).build()
  }
  lazy val configuration: Configuration = application.configuration
}
object Development extends Environment {
  override val configurationFile: String = "application.conf"
}
object Production extends Environment {
  override val configurationFile: String = "application.production.conf"
}
object Test extends Environment {
  override val configurationFile: String = "application.test.conf"
}
object Environment{
  def fromString(value: String): Environment = {
    value.toLowerCase() match {
      case "d" | "dev" | "development" => Development
      case "p" | "prod" | "production" => Production
      case "t" | "test" => Test
      case _=> throw new RuntimeException(f"no environment found: $value")
    }
  }
}