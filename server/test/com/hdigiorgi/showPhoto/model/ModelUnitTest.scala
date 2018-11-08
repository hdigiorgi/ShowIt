package com.hdigiorgi.showPhoto.model

import com.hdigiorgi.showPhoto.UnitTestBase
import org.scalatestplus.play.guice._
import play.api.test._
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelUnitTest extends FunSuite
                with GuiceOneAppPerTest with Injecting
                with UnitTestBase with Matchers{

  test("Smoke license persistence") {
    wrapCleanDB { db =>
      assert( true)
    }
  }

  test("crud license") {
    wrapCleanDB{ db =>
      val licenses = List (
        License(Grade(111), Free, Enabled),
        License(Grade(222), Price(3), Enabled),
        License(Grade(333), Price(4), Disabled)
      )
      licenses foreach { license =>
        // create
        db.license.read(license.grade) shouldBe empty
        db.license.update(license)
        val read = db.license.read(license.grade)
        read should not be empty
        read.get shouldEqual license

        // update
        val changed = license.copy(
          price = license.price.op(_+1),
          enabled = license.enabled.toggle)
        db.license.update(changed)
        val changedRead = db.license.read(license.grade)
        changedRead should not be empty
        changedRead.get shouldEqual changed

        // delete
        db.license.delete(license.grade)
        db.license.read(license.grade) shouldBe empty
      }
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

}
