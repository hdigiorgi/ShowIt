package com.hdigiorgi.showPhoto.model.payments

import com.hdigiorgi.showPhoto.application.Environment
import com.hdigiorgi.showPhoto.model.post.Post
import com.hdigiorgi.showPhoto.model.site.Site
import controllers.{UrlFormDecoder, routes}
import filters.TrackingHolder
import play.api.Configuration
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}

import scala.util.Try

package object paypal {

  case class IPN(map: Map[String, Seq[String]]) extends UrlFormDecoder(map){
    lazy val mc_gross: Try[Float] = F("mc_gross")
    lazy val payer_id: Try[String] = S("player_id")
    lazy val payment_status: Try[String] = S("payment_status")
    lazy val first_name: Try[String] = S("first_name")
    lazy val custom: Try[String] = S("custom")
    lazy val custom_ip: Try[String] = custom.flatMap{ custom =>
      Try(custom.split(" ")(0))
    }
    lazy val custom_tracking: Try[String] = custom.flatMap{ custom =>
      Try(custom.split(" ")(1))
    }
    lazy val verify_sign: Try[String] = S("verify_sign")
    lazy val payer_email: Try[String] = S("payer_email")
    lazy val item_name: Try[String] = S("item_name")
    lazy val item_number: Try[String] = S("item_number")
  }

  case class BuyFormData(site: Site, post: Post)(implicit cfg: Configuration, tracking: TrackingHolder) {

    def isSelling: Boolean = post.price.isDefined &&
      site.paypalEmail.value.isDefined

    def buyUrl: String = Environment().withValue(
      prod = "https://www.paypal.com/cgi-bin/webscr",
      dev = "https://www.sandbox.paypal.com/cgi-bin/webscr"
    )

    def item_name()(implicit i18n: Messages): String = {
      i18n("buy.filesOf") + " " + post.title.value
    }

    def item_number: String = post.id.value

    def custom: String = f"$ip $trackingCode"

    def amount: String = post.price.map(_.toString).getOrElse("")

    def business: String = site.paypalEmail.string

    def notifyUrl: String = genUrl(routes.PaypalController.IPN())

    def returnUrl: String = genUrl(routes.PaypalController.completed())

    def cancelUrl: String = genUrl(routes.PaypalController.cancelled())

    private def genUrl(relative: Call): String = {
      f"http://${tracking.requestHost}${relative.url}"
    }
    private def ip = tracking.userIp
    private def trackingCode = tracking.userTrackingCode
  }


  case class IPNVerificator(ipn: IPN) {

  }

}
