package controllers

import filters.Loged
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.Files
import play.api.mvc.{AnyContent, ControllerComponents, MultipartFormData}

class AdminSiteController @Inject()(cc: ControllerComponents)(implicit conf : Configuration)
    extends BaseController(cc) {

  def index() = Loged { Action { implicit r =>
    Ok(views.html.admin.site.edit(site))
  }}

  def saveName(): Loged[AnyContent] =
    argumented.updateFrom[String](Action, "site-name", siteManager.updateName)

  def saveDescription(): Loged[AnyContent] =
    argumented.updateFrom[String](Action, "site-description", siteManager.updateContent)

  def saveLinks(): Loged[AnyContent]=
    argumented.updateFrom[Seq[String]](Action, "site-links", siteManager.updateLinks)

  def imageProcess(): Loged[MultipartFormData[Files.TemporaryFile]]=
    multipart.receiveMultipart(parse, Action, new multipart.site.ImageReceiver(siteManager))

  def imageDelete(imageId: String): Loged[AnyContent] =
    multipart.deleteUploaded(Action, new multipart.site.ImageDeleter(siteManager, imageId))

  def imageLoad(imageId: String)(): Loged[AnyContent] =
    multipart.previewUpload(Action, new multipart.site.ImagePreviewer(siteManager, imageId))

  def imageList(): Loged[AnyContent] =
    multipart.listUploaded(Action, new multipart.site.ImageLister(siteManager))

}
