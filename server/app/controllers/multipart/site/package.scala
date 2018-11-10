package controllers.multipart

import java.io.File


import com.hdigiorgi.showPhoto.model.{ErrorMessage, FileSlug, Image}
import com.hdigiorgi.showPhoto.model.site.SiteManager
import controllers.routes
import play.api.Configuration

package object site {
  object Limits {
    val IMAGE_MAX_LENGTH_BYTES: Long = 35 * 1024 * 1024 // 35MB
  }

  class ImageReceiver(siteMgr: SiteManager)
    extends MultipartReceiver[Seq[Image]] {

    override def name: String = ""

    override val maxLengthBytes: Long = Limits.IMAGE_MAX_LENGTH_BYTES

    override def process(file: File, slug: FileSlug): Either[ErrorMessage, Seq[Image]] =
      siteMgr.processImage(file, slug)

    override def description(elements: Seq[Image]): FileDescription = {
      FileDescription(elements.head,
        Some(routes.AdminSiteController.imageLoad(elements.head.id)))
    }
  }

  class ImageDeleter(siteMgr: SiteManager, imageId: String)(implicit conf: Configuration)  extends MultiPartDeleter {
    override def name: String = imageId
    override def delete(): Either[ErrorMessage, Unit] = {
      siteMgr.deleteImage(FileSlug.noSlugify(imageId))
    }
  }

  class ImagePreviewer(siteMgr: SiteManager, imageId: String)(implicit conf: Configuration) extends MultipartPreviewer {
    override def name: String = imageId
    override def preview(): Option[File] = {
      siteMgr.getPreviewableImage(FileSlug.noSlugify(imageId))
    }
  }

  class ImageLister(siteMgr: SiteManager)(implicit conf: Configuration) extends MultipartLister {
    override def name: String = ""
    override def descriptions: Seq[FileDescription] = {
      siteMgr.listStoredImages() map { image =>
        FileDescription(image,
          Some(routes.AdminSiteController.imageLoad(image.id)))
      }
    }
  }

}
