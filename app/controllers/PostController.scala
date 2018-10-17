package controllers

import akka.stream.{KillSwitches, SharedKillSwitch}
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Configuration
import play.api.http.HttpEntity
import play.api.mvc.{AbstractController, ControllerComponents, ResponseHeader, Result}


import scala.collection.mutable

@Singleton
class PostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {

  def getImage(postId: String, size: String, imageName: String) = Action { r =>
    Ok("")
  }
  def getFile(postId: String, fileName: String) = Action { r =>
    Ok("")
  }


}

