package com.hdigiorgi.showPhoto.model.files

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import java.util.Random

import com.github.javafaker.Faker
import com.hdigiorgi.showPhoto.model.{FileSlug, StringId}
import javax.imageio.ImageIO
import org.apache.commons.io.FileUtils

import scala.util.Try

object RandomImage {

  def gen(): File = {
    val r      = new Random()
    val faker  = new Faker(r)
    val width  = (r.nextFloat() * 2000).toInt
    val height = (r.nextFloat() * 2000).toInt
    val name   = new Faker().dragonBall().character()
    gen(width, height, name)
  }

  def gen(width: Int, height: Int, name: String): File = {
    val buffer = genBufferedImage(width, height)
    saveBufferToFile(name, buffer)
  }

  def genAndSave(db: ImageFileDB ,id: StringId): Try[Seq[Image]] = storeImage(db, gen(), id)

  private def genBufferedImage(width: Int, height: Int): BufferedImage = {
    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    for(y <- Range(0, height)) {
      for(x <- Range(0, width)) {
        val a: Int = (Math.random()*256).toInt
        val r: Int = (Math.random()*256).toInt
        val g: Int = (Math.random()*256).toInt
        val b: Int = (Math.random()*256).toInt
        val pixel: Int = (a<<24) | (r<<16) | (g<<8) | b
        img.setRGB(x,y,pixel)
      }
    }
    img
  }

  private def saveBufferToFile(name: String, buffer: BufferedImage): File = {
    val fullName = System.currentTimeMillis().toString + "_" + FileSlug(name).withExtension(FORMAT).value
    val destination = Paths.get(FileUtils.getTempDirectoryPath, fullName).toFile
    ImageIO.write(buffer, FORMAT, destination)
    destination
  }

  private def storeImage(db: ImageFileDB, file: File, id: StringId): Try[Seq[Image]] ={
    db.process(file, id, FileSlug(file.getName))
  }

  private val FORMAT = "jpg"
}
