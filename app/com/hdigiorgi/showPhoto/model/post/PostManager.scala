package com.hdigiorgi.showPhoto.model.post

import com.hdigiorgi.showPhoto.model.files.{AttachmentFileDB, ImageFileDB}
import com.hdigiorgi.showPhoto.model._
import play.api.Configuration

class PostManager(val db: PostPI,
                  val imageDb: ImageFileDB,
                  val attachmentDb: AttachmentFileDB) {
  import PostManager.ErrorMessages

  def firstPostIfUnpublished: Option[Post] = {
    db.readPaginated(Page(number = 0, size = 1)) match {
      case Seq() => None
      case Seq(post, _*) => post.publicationStatus match {
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

  private def readPost(postId: String): Either[ErrorMessage, Post] = {
    db.read(postId) match {
      case None => ErrorMessages.UnexistentPost
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
    imageDb.getStoredImageIds(p.id) match {
      case Seq() => ErrorMessages.NoImages
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
    val UnexistentPost = Left(PostErrorMsg("validations.post.unexistent"))
    val NoImages = Left(ImageErrorMsg("validations.post.noImages"))
  }

}

