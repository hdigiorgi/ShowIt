package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class PaypalController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends BaseController(cc) {

  def IPN() = Action(parse.formUrlEncoded){request : Request[Map[String, Seq[String]]] =>
    logger.info(printableRequest(request))
    Ok("ipn")
  }

  def completed() = Action { request =>
    logger.info(printableRequest(request))
    Ok("completed")
  }

  def cancelled() = Action { request =>
    logger.info(printableRequest(request))
    Ok("cancelled")
  }



}
