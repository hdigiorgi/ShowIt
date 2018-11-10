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

  def genAndSave(configuration: Configuration, sampleImageFolder: String, amount: Integer): Seq[Post] = {
    val postDB = DBInterface.getDB()(configuration).post
    val imageDB = FileSystemInterface.get(configuration).postImage
    val sampleImageFolderFile = new File(sampleImageFolder)
    Range.inclusive(1,amount).map{ _ =>
      val post = genAndSave(postDB, imageDB, sampleImageFolderFile)
      println(post)
      post
    }
  }

  def genAndSave(postDB: PostPI, imageFileDB: ImageFileDB, sampleImageFolder: File): Post = {
    val post = Post().withTitle(genTitle).withRawContent(genContent).withPublicationStatus(Published)
    postDB.insert(post)
    Range(0,3).map(_ => genRandomImage(sampleImageFolder)).foreach{image =>
      imageFileDB.process(image, post.id, FileSlug(image.getName))
    }
    post
  }

  private def genTitle: String = {
    new Faker().book().title() + "_" + System.currentTimeMillis()
  }

  private def genContent: String = new Faker().lorem().paragraphs(5).asScala.foldLeft(new StringBuilder){ (builder, string) =>
    builder.append(string)
    builder.append(System.getProperty("line.separator"))
    builder
  }.toString()

  private def genRandomImage(sampleFolder: File): File = {
    if(!sampleFolder.exists() || !sampleFolder.isDirectory()) {
      throw new RuntimeException(f"folder ${sampleFolder.getCanonicalPath} doesn't exists or isn't a directory")
    }
    randomFromSamples(sampleFolder)
  }

  private def randomFromSamples(folder: File): File = {
    val files = folder.listFiles()
    val selected = files((Math.random()*files.length).toInt)
    val tempFile = Paths.get(FileUtils.getTempDirectory.getCanonicalPath,
      randomImageFileName(selected)).toFile
    FileUtils.copyFile(selected, tempFile)
    tempFile
  }

  private def randomImageFileName(file: File): String = {
    val ext = FilenameUtils.getExtension(file.getName)
    val name = System.currentTimeMillis().toString + "-" + new Faker().ancient().titan()
    f"$name.$ext"
  }

}
