package com.hdigiorgi.showPhoto.model.post

import java.io.File

import com.hdigiorgi.showPhoto.model.files.{AttachmentFileDB, ImageFileDB, SizeType}
import com.hdigiorgi.showPhoto.model._
import play.api.Configuration


class PostManager(val db: PostPI,
                  val imageDb: ImageFileDB,
                  val attachmentDb: AttachmentFileDB) {
  import PostManager.ErrorMessages._

  def posts(page: Int): PaginatedResult[Post] = {
    db.readPaginated(Page(page, 12)).map(post => {
      post.withImages(imageDb.getStoredImages(post.id))
    })
  }

  def imageFile(postId: StringId, imageSize: String, imageName: String): Either[ErrorMessage, File] = {
    val post = readPost(postId)
    if(post.isLeft) return Left(post.left.get)
    if(post.right.get.publicationStatus.isUnpublished) return PostIsUnpublished
    val sizeType = SizeType.fromString(imageSize)
    imageDb.getImageFileWithSuggestedSize(postId, sizeType, FileSlug(imageName)) match {
      case None => ImageNotFound
      case Some((imageFile,_)) => Right(imageFile)
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
    val ImageNotFound = Left(ImageErrorMsg("error.imageNotFound"))
  }

}

