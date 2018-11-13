package com.hdigiorgi.showPhoto.model

import com.hdigiorgi.showPhoto.UnitTestBase
import com.hdigiorgi.showPhoto.model.site.{Email, Site}
import org.scalatestplus.play.guice._
import play.api.test._
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelUnitTest extends FunSuite
                with GuiceOneAppPerTest with Injecting
                with UnitTestBase with Matchers{

  test("Smoke persistence") {
    wrapCleanDB { db =>
      assert( true)
    }
  }

  test("crud meta") {
    wrapCleanDB { db =>
      db.meta.read("A") shouldBe empty
      db.meta.update(Meta("A", "SOMETHING"))
      db.meta.read("A") should contain (Meta("A", "SOMETHING"))
      db.meta.update(Meta("A", "ELSE"))
      db.meta.read("A") should contain (Meta("A", "ELSE"))
      db.meta.delete("A")
      db.meta.read("A") shouldBe empty
    }
  }

  test("account crud") {
    wrapCleanDB{ db =>
      val acc1 = User(StringId.random, Email("a@a.com"), Password("1"))
      val acc2 = User(StringId.random, Email("b@b.com"), Password("2"))
      acc1.id shouldNot equal (acc2.id)
      db.user.read(acc1.id) shouldBe empty
      db.user.read(acc2.id) shouldBe empty
      db.user.insert(acc1)
      db.user.insert(acc2)
      db.user.read(acc1.id) should contain (acc1)
      db.user.read(acc2.id) should contain (acc2)
      db.user.delete(acc1.id)
      db.user.read(acc1.id) shouldBe empty
      db.user.read(acc2.id) should contain (acc2)
      db.user.delete(acc2.id)
      db.user.read(acc2.id) shouldBe empty
    }
  }

  test("site crud") {
    wrapCleanDB{ db =>
      db.site.read()
      val site = Site(name = "test", description="des", links= Seq("a","b"))
      db.site.update(site)
      db.site.read() shouldBe site
      val site2 = site.withStringLinks(Seq("x", "y", "z"))
      db.site.update(site2.right.get)
      db.site.read() shouldBe site2.right.get
      val siteWithPaypalEmail = site.withPaypalEmail("me@hdigiorgi.com")
      db.site.update(siteWithPaypalEmail.right.get)
      db.site.read() shouldBe siteWithPaypalEmail.right.get
    }
  }

}
