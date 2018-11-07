package controllers.multipart

import java.io.File

import com.hdigiorgi.showPhoto.model.files.FileEntry
import com.hdigiorgi.showPhoto.model.{ErrorMessage, FileSlug, Image, StringId}
import com.hdigiorgi.showPhoto.model.post.PostManager
import controllers.routes
import play.api.Configuration

package object post {
  object Limits {
    val IMAGE_MAX_LENGTH_BYTES: Long = 100 * 1024 * 1024 // 100MB
    val ATTACHMENT_MAX_LENGTH_BYTES: Long = 1024 * 1024 * 1024 // 1024MB - 1GB
  }

  class ImageReceiver(postId: String)(implicit conf : Configuration)
      extends MultipartReceiver[Seq[Image]] {

    override val maxLengthBytes: Long = Limits.IMAGE_MAX_LENGTH_BYTES

    override def process(file: File, slug: FileSlug): Either[ErrorMessage, Seq[Image]] =
      PostManager().processImage(postId, file, slug)

    override def description(elements: Seq[Image]): FileDescription = {
      FileDescription(elements.head,
        Some(routes.AdminPostController.imageLoad(postId, elements.head.id)))
    }

  }

  class AttachmentReceiver(postId: String)(implicit conf : Configuration)
    extends MultipartReceiver[FileEntry] {

    override val maxLengthBytes: Long = Limits.ATTACHMENT_MAX_LENGTH_BYTES

    override def process(file: File, slug: FileSlug): Either[ErrorMessage, FileEntry] =
      PostManager().processAttachment(postId, file, slug)

    override def description(element: FileEntry): FileDescription =
      FileDescription(element)

  }

  class ImageLister(postId: String)(implicit conf: Configuration) extends MultipartLister {
    override def descriptions: Seq[FileDescription] = {
      PostManager().adminListStoredImages(StringId(postId)) map {image =>
        FileDescription(image,
          Some(routes.AdminPostController.imageLoad(postId, image.id)))
      }
    }
  }

  class AttachmentLister(postId: String)(implicit conf: Configuration) extends MultipartLister {
    override def descriptions: Seq[FileDescription] = {
      PostManager().adminListStoredAttachments(StringId(postId)) map {FileDescription(_)}
    }
  }

  class ImageDeleter(postId: StringId, imageId: String)(implicit conf: Configuration)  extends MultiPartDeleter {
    override def name: String = imageId
    override def delete(): Either[ErrorMessage, Unit] = {
      PostManager().deleteImage(postId, FileSlug.noSlugify(imageId))
    }
  }

  class AttachmentDeleter(postId: StringId, attachmentId: String)(implicit conf: Configuration)  extends MultiPartDeleter {
    override def name: String = attachmentId
    override def delete(): Either[ErrorMessage, Unit] = {
      PostManager().deleteStoredAttachment(postId, FileSlug.noSlugify(attachmentId))
    }
  }

  class ImagePreviewer(postId: String, imageId: String)(implicit conf: Configuration) extends MultipartPreviewer {
    override def name: String = imageId
    override def preview(): Option[File] = {
      PostManager().getAdminPreviewableImage(StringId(postId), FileSlug.noSlugify(imageId))
    }
  }
}
