package com.hdigiorgi.showPhoto.model.post

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import com.hdigiorgi.showPhoto.model.{DBInterface, ErrorMessage, Page, PostPI}
import play.api.Configuration

class PostManager(private val db: PostPI) {

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

  def saveTitle(postId: String, title: String): Validated[ErrorMessage, Post] = {
    db.read(postId) match {
      case None => UnexistentPost
      case Some(post) =>
        val updated = post.withTitle(title)
        updated.title.validate(postId, db) match {
          case invalid @ Invalid(_) => invalid
          case Valid(_) =>
            db.update(updated)
            Valid(updated)
        }
    }
  }

  def saveContent(postId: String, content: String): Validated[ErrorMessage, Post] = {
    db.read(postId) match {
      case None => UnexistentPost
      case Some(post) =>
        val updated = post.withRawContent(content)
        db.update(updated)
        Valid(updated)
    }
  }

  private val UnexistentPost = Invalid(ErrorMessage("post.unexistent"))

}

object PostManager {
  def apply()(implicit cfg: Configuration): PostManager = {
    new PostManager(DBInterface.getDB().post)
  }
}
