package controllers

import com.hdigiorgi.showPhoto.model.ErrorMessage
import filters.{LanguageFilterSupport, Loged}
import org.apache.logging.log4j.Logger
import play.api.libs.json.Reads
import play.api.mvc._
import play.api.mvc.Results._

import scala.util.{Failure, Success, Try}

package object argumented {

  def updateFrom[A](action: ActionBuilder[Request, AnyContent],
                            field: String, savef: A => Either[ErrorMessage, _])
                           (implicit read : Reads[A], logger: Logger) =
    Loged {action { implicit request: Request[AnyContent] =>
      implicit val i18n = LanguageFilterSupport.messagesFromRequest(request)
      Try((request.body.asJson.get \ field).as[A]) match {
        case Failure(e) =>
          logger.error("can't parse request", e)
          BadRequest(e.getMessage)
        case Success(content) =>
          val opResult = savef(content)
          simpleResponse(opResult)
      }
    }}

  def simpleResponse(r: Either[ErrorMessage, _])(implicit i18n: play.api.i18n.Messages): Result = {
    r match {
      case Left(msg) => Conflict(msg.message)
      case Right(_) => Ok("{}")
    }
  }

}
