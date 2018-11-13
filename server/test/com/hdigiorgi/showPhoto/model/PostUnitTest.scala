package com.hdigiorgi.showPhoto.model
import java.time.Instant

import com.hdigiorgi.showPhoto.UnitTestBase
import com.hdigiorgi.showPhoto.model.post._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.sqlite.SQLiteException

@RunWith(classOf[JUnitRunner])
class PostUnitTest extends UnitTestBase{

  test("interface") {
    val post = Post("id1")
    val samePost = post.withCreationTime(post.creationTime)
    post shouldEqual samePost
    val titled = post.withTitle(Title("hello title"))
    titled.slug.value shouldEqual "hello_title"
  }

  test("markdown support") {
    val post = Post("id").withRawContent("# title")
    post.rawContent shouldEqual "# title"
    post.renderedContent.value shouldEqual "<h1>title</h1>"
    wrapCleanPostDB{db =>
      db.insert(post)
      val read = db.read("id").get
      read.rawContent shouldEqual post.rawContent
      read.renderedContent shouldEqual post.renderedContent
    }
  }

  test("basic crud") {
    wrapCleanPostDB{ db =>
      val post = Post().withTitle(Title("some title"))
      db.read(post.id) shouldBe empty
      db.insert(post)
      val readPost = db.read(post.id)
      readPost should not be empty
      readPost.get shouldEqual post
      val postNewTitle = post.withTitle(Title("new title 2"))
      db.update(postNewTitle)
      db.read(post.id).get shouldEqual postNewTitle
    }
  }

  test("read slug") {
    wrapCleanPostDB{ db =>
      val post1 = Post().withTitle(Title("test title"))
      val post2 = Post().withTitle(Title("other title"))
      db.insert(post1)
      db.insert(post2)
      val rPost1 = db.readBySlug(Slug.noSlugify("test_title"))
      val rPost2 = db.readBySlug(Slug.noSlugify("other_title"))
      rPost1.get shouldEqual post1
      rPost2.get shouldEqual post2
    }
  }

  test("same slug should fail") {
    wrapCleanPostDB{ db =>
      val post1 = Post(StringId("post1")).withTitle(Title("title"))
      db.insert(post1)

      val post2 = Post(StringId("post2")).withTitle(Title("title"))
      an [SQLiteException] should be thrownBy db.insert(post2)
    }
  }

  test("publication status") {
    wrapCleanPostDB { db =>
      val id = StringId("post_publication_status")
      db.insert(Post(id))
      val post = db.read(id).get
      post.publicationStatus shouldBe Unpublished
      db.update(post.togglePublicationStatus)
      val read = db.read(id).get
      post.id shouldEqual read.id
      post.publicationStatus.toggle shouldBe read.publicationStatus
      read.publicationStatus shouldBe Published
    }
  }


  test("post fetching order") {
    val toInsert = Range(10,0).map{ i =>
      Post(i.toString)
        .withCreationTime(Instant.ofEpochSecond(i*1000))
        .withSlug(i.toString)
    }
    val insertedPage1 = toInsert.take(5)
    val insertedPage2 = toInsert.drop(5)
    wrapCleanPostDB{ db =>
      toInsert foreach db.insert
      val readPage1 = db.readPaginated(Page(index=0, size= 5)).elements
      val readPage2 = db.readPaginated(Page(index=1, size=5)).elements
      val readPage6 = db.readPaginated(Page(index=5, size=5)).elements
      insertedPage1 shouldEqual readPage1
      insertedPage2 shouldEqual readPage2
      readPage6 shouldEqual Seq.empty
    }
  }

  test("price") {
    wrapCleanPostDB{ db =>
      val p2 = Post().withPrice(Price(2))
      val p3 = p2.withPrice(Price(3))
      db.insert(p2)
      val readP2 = db.read(p2.id).get
      readP2 shouldBe p2
      readP2 should not be p3
      db.update(p3)
      val readP3 = db.read(p2.id).get
      readP3 should not be p2
      readP3 shouldBe p3
    }
  }

}
