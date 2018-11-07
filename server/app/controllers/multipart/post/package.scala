package controllers.multipart

import java.io.File
import com.hdigiorgi.showPhoto.model.{ErrorMessage, FileSlug, Image, StringId}
import com.hdigiorgi.showPhoto.model.post.PostManager
import controllers.routes
import play.api.Configuration

package object post {
  class ImageReceiver(postId: String)(implicit conf : Configuration)
      extends MultipartReceiver[Seq[Image]] {

    override val maxLengthBytes: Long = 100 * 1024 * 1024 // 100MB

    override def process(file: File, slug: FileSlug): Either[ErrorMessage, Seq[Image]] =
      PostManager().processImage(postId, file, slug)

    override def description(elements: Seq[Image]): FileDescription = {
      FileDescription(elements.head,
        Some(routes.AdminPostController.imageLoad(postId, elements.head.id)))
    }

  }

  class ImageLister(postId: String)(implicit conf: Configuration) extends MultipartLister {
    override def descriptions: Seq[FileDescription] = {
      PostManager().listStoredImages(StringId(postId)) map {image =>
        FileDescription(image,
          Some(routes.AdminPostController.imageLoad(postId, image.id)))
      }
    }
  }

  class ImageDeleter(postId: StringId, imageId: String)(implicit conf: Configuration)  extends MultiPartDeleter {
    override def name: String = imageId
    override def delete(): Either[ErrorMessage, Unit] = {
      PostManager().deleteImage(postId, FileSlug.noSlugify(imageId))
    }
  }

  class ImagePreviewer(postId: String, imageId: String)(implicit conf: Configuration) extends MultipartPreviewer {
    override def name: String = imageId
    override def preview(): Option[File] = {
      PostManager().getAdminPreviewableImage(StringId(postId), FileSlug.noSlugify(imageId))
    }
  }
}
