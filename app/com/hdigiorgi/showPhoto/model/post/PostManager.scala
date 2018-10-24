package com.hdigiorgi.showPhoto.model.post

import com.hdigiorgi.showPhoto.model.{DBInterface, Page, PostPI}
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

}

object PostManager {
  def apply()(implicit cfg: Configuration): PostManager = {
    new PostManager(DBInterface.getDB().post)
  }
}
