package com.hdigiorgi.showPhoto.model

import com.hdigiorgi.showPhoto.model.post.{Post, PostManager, Published}
import test.TestBase

class PostManagerTest extends TestBase {
  test("first post if unpublished") {
    wrapCleanPostDB{ db =>
      val pm = new PostManager(db)
      pm.firstPostIfUnpublished shouldBe empty
      val p1 = Post("p1").withTitle("title_1")
      db.insert(p1)
      pm.firstPostIfUnpublished.get shouldBe p1
      val p1Published = p1.withPublicationStatus(Published)
      db.update(p1Published)
      pm.firstPostIfUnpublished shouldBe empty
      val p2 = pm.firsPostIfUnpublishedCreateNewOtherwise()
      val newFirstUnpublished = pm.firstPostIfUnpublished.get
      newFirstUnpublished shouldBe p2
    }
  }
}
