package com.hdigiorgi.showPhoto.model.payments

import com.hdigiorgi.showPhoto.application.Environment
import com.hdigiorgi.showPhoto.model.post.{Post, Price}
import com.hdigiorgi.showPhoto.model.purchase.PurchaseManager
import com.hdigiorgi.showPhoto.model.site.Site
import controllers.{UrlFormDecoder, routes}
import filters.TrackingHolder
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client._
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
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

    def verify(request: Request[Map[String, Seq[String]]])(implicit cfg: Configuration): Try[Boolean] = Try {
      val httpClient = createHttpClient()
      val httpPost = new HttpPost(verifyUrl())
      httpPost.setEntity(toUrlEncodedEntity(request.body))
      val response = httpClient.execute(httpPost)
      val entityResponse = response.getEntity
      val responseBodyString = EntityUtils.toString(entityResponse)
      EntityUtils.consume(entityResponse)
      httpClient.close()
      responseBodyString.trim.equals("VERIFIED")
    }

    private def createHttpClient(): CloseableHttpClient = {
      HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy)
        .setRetryHandler(new DefaultHttpRequestRetryHandler(10, true))
        .setUserAgent("Scala-IPN-VerificationScript")
        .build()
    }

    private def toUrlEncodedEntity(data: Map[String, Seq[String]]): UrlEncodedFormEntity = {
      val params = new java.util.ArrayList[BasicNameValuePair]
      for((key, seq) <- data) {
        val value = seq.reduce((a,b) => a + b)
        params.add(new BasicNameValuePair(key, value))
      }
      params.add(new BasicNameValuePair("cmd", "_notify-validate"))
      new UrlEncodedFormEntity(params)
    }

    private def verifyUrl()(implicit cfg: Configuration) = {
      Environment().withValue(
        prod = "https://ipnpb.paypal.com/cgi-bin/webscr",
        dev = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"
      )
    }
  }

  case class BuyFormData(site: Site, post: Post, purchaseManager: PurchaseManager)(implicit cfg: Configuration, tracking: TrackingHolder) {
    val price: Option[Price] = post.price.map(_.withPercentageFee(0.05f).withFixedFee(0.3f))

    lazy val isDownloadable: Boolean = post.attachments.nonEmpty
    lazy val wasBought: Boolean = purchaseManager.hasValidPurchase(post.id, tracking).isRight

    lazy val isSelling: Boolean = isDownloadable &&
      price.isDefined &&
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

    def amount: String = price.map(_.baseValue.toString).getOrElse("")

    def tax: String = price.map(_.fees.toString).getOrElse("")

    def business: String = site.paypalEmail.string

    def notifyUrl: String = genUrl(routes.PaypalController.IPN())

    def returnUrl: String = genUrl(routes.PaypalController.completed(post.id))

    def cancelUrl: String = genUrl(routes.PaypalController.cancelled(post.id))

    private def genUrl(relative: Call): String = {
      f"http://${tracking.requestHost}${relative.url}"
    }

    private def ip = tracking.userIp

    private def trackingCode = tracking.userTrackingCode
  }

}
