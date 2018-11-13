package controllers

import com.hdigiorgi.showPhoto.model.payments.paypal
import com.hdigiorgi.showPhoto.model.purchase.PurchaseManager
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AnyContent, ControllerComponents, Request}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PaypalController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
  extends BaseController(cc) {

  def IPN() = Action(parse.formUrlEncoded){request : Request[Map[String, Seq[String]]] =>
    Future{processIPN(request)}
    Ok("")
  }

  def completed(postId: String) = Action { request =>
    Redirect(routes.PostController.waitForDownload(postId))
  }

  def cancelled(postId: String) = Action { request =>
    Redirect(routes.PostController.downloadCancelled(postId))
  }

  private def processIPN(request : Request[Map[String, Seq[String]]]): Unit = {
    val ipn = paypal.IPN(request.body)
    ipn.verify(request) match {
      case Failure(t) => logError(request, t)
      case Success(verified) => verified match {
        case false =>
          logger.error("IPN was invalid")
          logError(request)
        case true =>
          val createdPurchase = purchaseManager.createPurchase(ipn)
          logger.info(f"New purchase registered $createdPurchase")
      }
    }
  }

  private def logError(request : Request[Map[String, Seq[String]]], throwable: Throwable = null): Unit = {
    logger.error(request)
    if(throwable!=null){
      logger.error(throwable)
    }
    logger.error(request.body.toList)
  }

  private val purchaseManager = PurchaseManager()
}
