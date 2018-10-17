package com.hdigiorgi.showPhoto.model
import org.scalatestplus.play.guice._
import play.api.test._
import org.scalatest._
import Matchers._

class ModelTest extends FunSuite
                with GuiceOneAppPerTest with Injecting
                with test.UseTestConfig with Matchers {

  test("Smoke license persistence") {
    DBInterface.wrapCleanDB { db =>
      assert( true)
    }
  }

  test("crud license") {
    DBInterface.wrapCleanDB{ db =>
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
    DBInterface.wrapCleanDB { db =>
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
    DBInterface.wrapCleanDB{ db =>
      val admin = User(StringId.random, Email("a@b.com"), Password("p"), Role.Admin)
      db.user.update(admin)
      db.user.read(admin.id) should contain (admin)
      db.user.delete(admin.id)
      db.user.read(admin.id) shouldBe empty
    }
  }

}
