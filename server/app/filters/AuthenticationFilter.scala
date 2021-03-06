package filters

import com.hdigiorgi.showPhoto.model._
import akka.stream.Materializer
import com.google.inject.Inject
import controllers.routes
import play.api.Configuration
import play.api.libs.typedmap.TypedKey
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object AuthenticationFilter {
  val idKeyName = "auth"
  val User: TypedKey[Cell[User]] = TypedKey.apply[Cell[User]]("messages")
}

class AuthenticationSupportHolder(val user: Option[User]) {
  def isAuthenticated: Boolean = user match {
    case None => false
    case Some(_) => true
  }
}

class AuthenticationResultHolder(val originalResult: Result) {
  def withAuthenticated(user: User)(implicit rh: RequestHeader): Result = {
    originalResult.addingToSession((AuthenticationFilter.idKeyName, user.id.value))
  }
  def deauthenticate()(implicit rh: RequestHeader): Result = {
    originalResult.removingFromSession(AuthenticationFilter.idKeyName)
  }
}

trait AuthenticationSupport {
  implicit def request2Holder(rh: RequestHeader): AuthenticationSupportHolder = {
    val user = rh.attrs.get(AuthenticationFilter.User).map(_.value  )
    new AuthenticationSupportHolder(user)
  }
  implicit def result2Holder(r: Result): AuthenticationResultHolder = {
    new AuthenticationResultHolder(r)
  }
}

class AuthenticationFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext, cfg: Configuration) extends Filter{

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    rh.session.get(AuthenticationFilter.idKeyName).flatMap(getUser) match {
      case None =>
        f(removeKeyFromAttrs(rh, AuthenticationFilter.idKeyName))
      case Some(user) =>
        f(rh.addAttr(AuthenticationFilter.User, Cell[User](user)))
    }
  }

  private def getUser(id: String)(implicit cfg: Configuration): Option[User] = {
    DBInterface.wrap{ db =>
      db.user.read(StringId(id))
    }
  }

  private def removeKeyFromAttrs(rh: RequestHeader, key: String): RequestHeader = {
    val session = rh.attrs(RequestAttrKey.Session).value
    val updatedSession = session - key
    rh.addAttr(RequestAttrKey.Session, Cell[Session](updatedSession))
  }

}

case class Loged[A](action: Action[A]) extends Action[A] with AuthenticationSupport{

  def apply(request: Request[A]): Future[Result] = {
    if (request.isAuthenticated) {
      action(request)
    } else {
      Future.successful(Results.Redirect(routes.AuthenticationController.index()))
    }
  }

  override def parser: BodyParser[A] = action.parser
  override def executionContext: ExecutionContext = action.executionContext
}
