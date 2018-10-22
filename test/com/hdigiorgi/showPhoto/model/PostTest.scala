package com.hdigiorgi.showPhoto.model
import org.scalatestplus.play.guice._
import play.api.test._
import org.scalatest._
import Matchers._
import com.hdigiorgi.showPhoto.model.post.{Post, Title}

class PostTest extends FunSuite
               with GuiceOneAppPerTest with Injecting
               with test.UseTestConfig with Matchers {

  test("interface") {
    val post = Post(StringId("id1"))
    val samePost = Post(StringId("id1")).setCreationTime(post.creationTime)
    post shouldEqual samePost
    post.setTitle(Title("hello title"))
    post.slug.value shouldEqual "hello_title"
  }

  test("basic crud") {
    DBInterface.wrapCleanDB{ dbi =>
      val db = dbi.post
      val post = Post().setTitle(Title("some title"))
      db.read(post.id) shouldBe empty
      db.update(post)
      val readPost = db.read(post.id)
      readPost should not be empty
      readPost.get shouldEqual post
      post.setTitle(Title("new title 2"))
      db.update(post)
      db.read(post.id).get shouldEqual post
    }
  }

  test("read slug") {
    DBInterface.wrapCleanDB{ dbi =>
      val db = dbi.post
      val post1 = Post().setTitle(Title("test title"))
      val post2 = Post().setTitle(Title("other title"))
      db.update(post1)
      db.update(post2)
      val rPost1 = db.readBySlug(Slug.noSlugify("test_title"))
      val rPost2 = db.readBySlug(Slug.noSlugify("other_title"))
      rPost1.get shouldEqual post1
      rPost2.get shouldEqual post2
    }
  }

  test("same slug should fail") {
    DBInterface.wrapCleanDB{ dbi =>
      val db = dbi.post
      val post1 = Post(StringId("post1")).setTitle(Title("title"))
      db.update(post1)

      val post2 = Post(StringId("post2")).setTitle(Title("title"))
      an [Exception] should be thrownBy db.update(post2)

    }
  }




}
