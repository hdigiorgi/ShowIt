package com.hdigiorgi.showPhoto.model

import java.time.{Duration, Instant}

import cats.data.Validated.{Invalid, Valid}
import com.hdigiorgi.showPhoto.model.files.RandomImage
import com.hdigiorgi.showPhoto.model.post._
import test.TestBase

class PostManagerTest extends TestBase {
  test("first post if unpublished") {
    wrapCleanDB{ db =>
      val pm = PostManager(db)
      pm.firstPostIfUnpublished shouldBe empty
      val p1 = Post("p1").withTitle("title_1")
      db.post.insert(p1)
      pm.firstPostIfUnpublished.get shouldBe p1
      val p1Published = p1.withPublicationStatus(Published)
      db.post.update(p1Published)
      pm.firstPostIfUnpublished shouldBe empty
      val p2 = pm.firsPostIfUnpublishedCreateNewOtherwise()
      val newFirstUnpublished = pm.firstPostIfUnpublished.get
      newFirstUnpublished shouldBe p2
    }
  }

  test("update title") {
    wrapPostManager{ pm =>
      pm.saveTitle("no_there", "a") shouldBe PostManager.ErrorMessages.UnexistentPost
      val post = pm.firsPostIfUnpublishedCreateNewOtherwise()
      pm.saveTitle(post.id, "aa") shouldBe Title.ErrorMessages.ToShort
      pm.saveTitle(post.id, "long" * 26) shouldBe Title.ErrorMessages.ToLong
      pm.saveTitle(post.id, "l    l" ) shouldBe Title.ErrorMessages.LeadToShortSlug
      pm.saveTitle(post.id, "Some VALID title").isRight shouldBe true
      pm.unpublish(post.id) shouldBe PublicationStatus.ErrorMessages.AlreadyInThatState
      pm.publish(post.id) shouldBe PostManager.ErrorMessages.NoImages
      RandomImage.genAndSave(pm.imageDb, post.id)
      pm.publish(post.id).isRight shouldBe true
      pm.publish(post.id) shouldBe PublicationStatus.ErrorMessages.AlreadyInThatState
    }
  }

  test("update content") {

  }

}
