package com.hdigiorgi.showPhoto.model
import org.scalatestplus.play.guice._
import play.api.test._
import org.scalatest._
import Matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.hdigiorgi.showPhoto.model.post.{Post, Published, Title, Unpublished}
import org.sqlite.SQLiteException


@RunWith(classOf[JUnitRunner])
class PostTest extends FunSuite
               with GuiceOneAppPerTest with Injecting
               with test.UseTestConfig with Matchers {

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

}
