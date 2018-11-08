package com.hdigiorgi.showPhoto.model.post

import java.io.File

import com.hdigiorgi.showPhoto.model.files._
import com.hdigiorgi.showPhoto.model._
import play.api.Configuration

import scala.util.{Failure, Success}


class PostManager(val db: PostPI,
                  val imageDb: ImageFileDB,
                  val attachmentDb: AttachmentFileDB) {
  import PostManager.ErrorMessages._

  def post(slug: Slug): Option[Post] = {
    db.readBySlug(slug).filter(_.publicationStatus.isPublished).map{post =>
      post.withImages(imageDb.getStoredImages(post.id))
    }
  }

  def adminGetPostById(postId: StringId): Option[Post] = {
    db.read(postId)
  }

  def publishedPosts(page: Int): PaginatedResult[Post] = {
    db.readPaginated(Page(page, 12), Some(Published)).map(post => {
      post.withImages(imageDb.getStoredImages(post.id))
    })
  }

  def getImageFile(postId: StringId, imageSize: String, imageName: String): Either[ErrorMessage, File] = {
    val post = readPost(postId)
    if(post.isLeft) return Left(post.left.get)
    if(post.right.get.publicationStatus.isUnpublished) return PostIsUnpublished
    val sizeType = SizeType.fromString(imageSize)
    imageDb.getImageFileWithSuggestedSize(postId, sizeType, FileSlug(imageName)) match {
      case None => ImageNotFound
      case Some((imageFile,_)) => Right(imageFile)
    }
  }

  def processImage(postId: StringId, file: File, fileName: FileSlug): Either[ErrorMessage, Seq[Image]] = {
    imageDb.process(file, postId, fileName) match {
      case Failure(exception) => ErrorProcessingImage
      case Success(result) => Right(result)
    }
  }

  def adminListStoredImages(postId: StringId): Seq[Image] = imageDb.getStoredImages(postId)

  def deleteImage(postId: StringId, name: FileSlug): Either[ErrorMessage, Unit] = {
    imageDb.getStoredImages(postId) match {
      case Seq(_) => OneImageNeeded
      case Seq(_, _*) =>
        imageDb.deleteImage(postId, name) match {
          case false => ImageNotFound
          case true => Right(())
        }
    }
  }

  def getAdminPreviewableImage(postId: StringId, name: FileSlug): Option[File] = {
    imageDb.getImageFileWithSuggestedSize(postId, SmallSize, name).map(_._1)
  }

  def processAttachment(postId: StringId, file: File, name: FileSlug): Either[ErrorMessage, FileEntry] = {
    attachmentDb.addFile(postId, file, name.value) match {
      case Failure(exception) => Left(FatalErrorMsg(exception))
      case Success(entry) => Right(entry)
    }
  }

  def adminListStoredAttachments(postId: StringId): Seq[FileEntry] =
    attachmentDb.listFiles(postId).getOrElse(Seq.empty)

  def deleteStoredAttachment(postId: StringId, file: FileSlug): Either[ErrorMessage, Unit] = {
    attachmentDb.removeFile(postId, file.value) match {
      case Failure(exception) => Left(FatalErrorMsg(exception))
      case Success(_) => Right(())
    }
  }

  def firstPostIfUnpublished: Option[Post] = {
    db.readPaginated(Page(index = 0, size = 1)).firstOption.flatMap{ post =>
      post.publicationStatus match {
        case Unpublished => Some(post)
        case _ => None
      }
    }
  }

  def firsPostIfUnpublishedCreateNewOtherwise(): Post = {
    firstPostIfUnpublished match {
      case Some(post) => post
      case None =>
        val newPost = Post()
        db.insert(newPost)
        newPost
    }
  }

  def saveTitle(postId: String, title: String): Either[ErrorMessage, Post] = for {
    post <- readPost(postId)
    _ <- Title(title).validate(postId, db)
  } yield {
    val updated = post.withTitle(title)
    db.update(updated)
    updated
  }

  def saveContent(postId: String, content: String): Either[ErrorMessage, Post] = for {
    post <- readPost(postId)
  }yield {
    val updated = post.withRawContent(content)
    db.update(updated)
    updated
  }

  def publish(postId: String): Either[ErrorMessage, Post] = for {
    post <- readPost(postId)
    _ <- post.title.validate(postId, db)
    _ <- post.publicationStatus.validateToggle(Published)
    _ <- validatePostImages(post)
  } yield {
    val published = post.togglePublicationStatus
    db.update(published)
    published
  }

  def unpublish(postId: String): Either[ErrorMessage, Post] = for {
    post <- readPost(postId)
    _ <- post.publicationStatus.validateToggle(Unpublished)
  } yield {
    val updated = post.togglePublicationStatus
    db.update(updated)
    updated
  }

  def delete(postId: String): Either[ErrorMessage, Post] = for {
    post <- readPost(postId)
  } yield {
    db.delete(postId)
    post
  }

  private def readPost(postId: String): Either[ErrorMessage, Post] = {
    db.read(postId) match {
      case None => UnexistentPost
      case Some(post) => Right(post)
    }
  }

  private def validatePostBeforePublished(p: Post): Either[ErrorMessage, Post] = {
    for {
      _ <- p.title.validate(p.id, db)
      r <- validatePostImages(p)
    } yield r
  }

  private def validatePostImages(p: Post): Either[ErrorMessage, Post] = {
    imageDb.getStoredImages(p.id) match {
      case Seq() => NoImages
      case _ => Right(p)
    }
  }

}

object PostManager {

  def apply()(implicit cfg: Configuration): PostManager = {
    new PostManager(DBInterface.getDB().post, new ImageFileDB(), new AttachmentFileDB())
  }

  def apply(db: DBInterface)(implicit cfg: Configuration): PostManager = {
    new PostManager(db.post, new ImageFileDB(), new AttachmentFileDB())
  }

  object ErrorMessages {
    val UnexistentPost = Left(PostErrorMsg("validations.unexistent"))
    val PostIsUnpublished = Left(PostErrorMsg("error.unpublished"))
    val NoImages = Left(ImageErrorMsg("validations.noImages"))
    val OneImageNeeded = Left(ImageErrorMsg("validations.oneImageNeeded"))
    val ErrorProcessingImage = Left(ImageErrorMsg("error.processFailure"))
    val ErrorProcessingAttachment = Left(AttachmentErrorMsg("error.processFailure"))
    val ImageNotFound = Left(ImageErrorMsg("error.imageNotFound"))
  }

}

