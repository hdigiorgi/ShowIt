package controllers

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import filters.WhenAdmin
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

class AdminPostController @Inject()(cc: ControllerComponents)(implicit conf : Configuration) extends AbstractController(cc) {

  def index(page: Option[Integer], order: Option[String], search: Option[String]) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def create() = WhenAdmin { Action { implicit request: Request[AnyContent] =>
      Ok(views.html.admin.post.edit()).withHeaders(SecurityHeadersFilter.CONTENT_SECURITY_POLICY_HEADER -> "")
  }}

  def edit(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def save(id: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.admin.post.index())
  }

  def imageProcess(id: String) = WhenAdmin { Action(parse.temporaryFile) { request =>
    val atomicMove = StandardCopyOption.ATOMIC_MOVE
    val from = request.body.path
    val to = new File("").toPath
    Files.move(from, to, atomicMove)
    Ok("ok")
  }}

  def imageDelete(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}


  def imageRevert(id: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageLoad(id: String, load: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageRestore(id: String, restore: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

  def imageFetch(id: String, fetch: String) = WhenAdmin { Action { _ =>
    Ok("ok")
  }}

}
