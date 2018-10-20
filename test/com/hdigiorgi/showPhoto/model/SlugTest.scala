package com.hdigiorgi.showPhoto.model

import play.api.test._
import org.scalatest._
import Matchers._

class SlugTest extends FunSuite with Matchers  {

  test("normal slug") {
    Slug("").value shouldBe ""
    Slug("general! t.es.t").value shouldBe "general_t_es_t"
    Slug("  empty   space    ").value shouldBe "empty_space"
    Slug("transliteration 雞").value shouldBe "transliteration_ji"
    Slug("sep-arated").value shouldBe "sep_arated"
  }

  test("file slug") {
    FileSlug("data.zip").value shouldBe "data.zip"
    FileSlug("sep-arated").value shouldBe "sep_arated"
    FileSlug("data...zip").value shouldBe "data.zip"
    FileSlug("data.zip").withExtension("rar").value shouldBe "data.rar"
    FileSlug("data.zip").withExtension(".rar").value shouldBe "data.rar"
    FileSlug("data.zip").withExtension(".tar.gz").value shouldBe "data.tar.gz"
    FileSlug("A-B c_d s.jpg").value shouldBe "a_b_c_d_s.jpg"
    FileSlug("data.zip").withPrefix("prefix").value shouldBe "prefix_data.zip"
    FileSlug("data.zip").withPrefix("12").value shouldBe "12_data.zip"
    FileSlug("basename.of.data").baseName shouldBe "basename"
  }

}
