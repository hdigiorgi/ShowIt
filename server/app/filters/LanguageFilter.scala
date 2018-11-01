package filters

import javax.inject.Inject
import akka.stream.Materializer
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import com.hdigiorgi.showPhoto.model.Language
import play.api.i18n.{Lang, Langs, Messages, MessagesApi}
import play.api.libs.typedmap.TypedKey

object LanguageFilter {
  val Messages: TypedKey[Messages] = TypedKey.apply[Messages]("messages")
}

trait LanguageFilterSupport {
  implicit def request2Messages(implicit request: RequestHeader): Messages = {
    request.attrs.get(LanguageFilter.Messages).get
  }
}

class LanguageFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext, langs: Langs, messagesApi: MessagesApi) extends Filter {
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val lang = getLanguageFromHeader(requestHeader, langs)
    val msgs = messagesApi.preferred(Seq(lang))
    val header = requestHeader.addAttr(LanguageFilter.Messages, msgs)

    nextFilter(header)
  }

  def getLanguageFromHeader(h: RequestHeader, langs: Langs): Lang = {
    h.acceptLanguages.find(lang => Language.toLanguage(lang.code).isDefined).getOrElse{
      langs.availables.head
    }
  }

}
