package com.hdigiorgi.showPhoto.model.post

import java.io.File
import java.nio.file.Paths

import com.github.javafaker.Faker
import com.hdigiorgi.showPhoto.model.{DBInterface, FileSlug, PostPI}
import com.hdigiorgi.showPhoto.model.files.{FileSystemInterface, ImageFileDB, RandomImage}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import play.api.Configuration

import collection.JavaConverters._

object RandomPost {

  def genAndSave(configuration: Configuration, sampleImageFolder: String, atachmentFolder: String, amount: Integer): Seq[Post] = {
    val postManager = PostManager()(configuration)
    val sampleImageFolderFile = new File(sampleImageFolder)
    val attachmentFolderFile = new File(atachmentFolder)
    Range.inclusive(1,amount).map{ _ =>
      val post = genAndSave(postManager, sampleImageFolderFile, attachmentFolderFile)
      println(post)
      post
    }
  }

  def genAndSave(mgr: PostManager, sampleImageFolder: File, attachmentFolder: File): Post = {
    val post = mgr.firsPostIfUnpublishedCreateNewOtherwise().id
    mgr.saveTitle(post, genTitle())
    mgr.saveContent(post, genContent())

    for(_ <- 1 to 3) {
      val image = getRandomFileFromSamples(sampleImageFolder)
      mgr.processImage(post, image, FileSlug(image.getName))
    }

    for(_ <- 1 to 7) {
      val attachment = getRandomFileFromSamples(attachmentFolder)
      mgr.processAttachment(post, attachment, FileSlug(attachment.getName))
    }

    mgr.publish(post).right.get
  }

  private def genTitle(): String = {
    new Faker().book().title() + " " +
    new Faker().ancient().god() + " " +
    new Faker().ancient().hero() + " " +
    new Faker().overwatch().hero()
  }

  private def genContent(): String = new Faker().lorem().paragraphs(5).asScala.foldLeft(new StringBuilder){ (builder, string) =>
    builder.append(string)
    builder.append(System.getProperty("line.separator"))
    builder.append(System.getProperty("line.separator"))
    builder
  }.toString()

  private def getRandomFileFromSamples(sampleFolder: File): File = {
    if(!sampleFolder.exists() || !sampleFolder.isDirectory) {
      throw new RuntimeException(f"folder ${sampleFolder.getCanonicalPath} doesn't exists or isn't a directory")
    }
    getRandomFileFromSamplesUnchecked(sampleFolder)
  }

  private def getRandomFileFromSamplesUnchecked(folder: File): File = {
    val files = folder.listFiles()
    val selected = files((Math.random()*files.length).toInt)
    val tempFile = Paths.get(FileUtils.getTempDirectory.getCanonicalPath,
      randomImageFileName(selected)).toFile
    FileUtils.copyFile(selected, tempFile)
    tempFile
  }

  private def randomImageFileName(file: File): String = {
    val ext = FilenameUtils.getExtension(file.getName)
    val name = new Faker().app().name() + " " + new Faker().artist().name() + " " + new Faker().ancient().titan()
    f"$name.$ext"
  }

}
