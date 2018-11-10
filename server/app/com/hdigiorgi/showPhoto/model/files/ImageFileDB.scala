package com.hdigiorgi.showPhoto.model.files

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.Instant
import scala.collection.JavaConverters._
import com.hdigiorgi.showPhoto.model._
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.apache.commons.lang3.StringUtils
import org.im4java.core._
import org.im4java.process.ArrayListOutputConsumer
import play.api.Configuration
import scala.util.{Failure, Success, Try}


case class SizeType(name: String, value: Integer, quality: Integer, blur: Integer = 0) {
  def >(that: SizeType): Boolean = this.value > that.value
  def <(that: SizeType): Boolean = this.value < that.value
}
object SizeType {
  lazy val sizes = Seq(FullSize, MediumSize, SmallSize, BlurSize)
  def fromString(name: String): SizeType = {
    sizes.find(_.name.equalsIgnoreCase(name)).getOrElse(SmallSize)
  }
}
object FullSize extends SizeType("full", 2560, 75)
object MediumSize extends SizeType("medium", 1280, 45)
object SmallSize extends SizeType("small", 640, 30)
object BlurSize extends SizeType("blur", 200, 100, 100)

case class ImageSize(x: Integer, y: Integer) {
  def getDownScaled(value: Int): ImageSize = {
    val min = Math.min(this.x, this.y)
    val correction = min - value
    if(correction < 0) {
      this
    } else{
      ImageSize(this.x - correction, this.y - correction)
    }
  }

  def >(value: Integer): Boolean = {
    this.x > value || this.y > value
  }

  def <(value: Integer): Boolean = {
    this.x < value || this.y < value
  }
}

trait ImageTransformationError
object InvalidOutputExtension extends ImageTransformationError
case class ImageMagickExecutionError(e: Exception) extends ImageTransformationError

trait FileInterface {
  def getNewLocation(container: StringId, fileName: String): Path
  def getFiles(container: StringId): Seq[File]
}

abstract class ImageFileDB()(implicit private val cfg: Configuration){

  def location: String = filesRoot

  def process(tempInputFile: File, elementId: StringId, inFileName: FileSlug): Try[Seq[Image]] = {
    val toProcess = renameWithFileExtension(tempInputFile, inFileName)
    val destinationSlug = getUniqueFileSlug(elementId, inFileName)
    val imageProcessResult = processImageBySize(toProcess, elementId, destinationSlug)
    val paletteProcessResult = processImagePalette(elementId, destinationSlug)
    val processResult = imageProcessResult.flatMap(_ => paletteProcessResult)
    FileUtils.forceDelete(toProcess)

    processResult match {
      case Failure(t) =>
        deleteImageDir(elementId, destinationSlug)
        Failure(t)
      case Success(_) =>
        imageProcessResult.flatMap(sizesToImages(elementId, destinationSlug, _))
    }
  }

  def getStoredImages(elementId: StringId): Seq[Image] = {
    getContainerFolders(elementId).map(imageSlug => {
      getImage(elementId, FileSlug(imageSlug.getName))
    }).filter(_.isDefined).map(_.get)
  }

  def getImage(elementId: StringId, imageSlug: FileSlug): Option[Image] = {
    getPalette(elementId, imageSlug).map{palette =>
      Image(elementId, imageSlug, palette)
    }.toOption
  }

  def getImageFileWithSuggestedSize(elementId: StringId, size: SizeType, image: FileSlug): Option[(File, SizeType)] = {
    val targetFile = getImageLocation(elementId, size, image)
    if(targetFile.exists()) return Some((targetFile, size))

    val options = SizeType.sizes.filter(_> size).reverse ++ SizeType.sizes.filter(_< size)
    options.foldLeft(None: Option[(File, SizeType)])((acc, size) => acc match {
      case Some(x) => Some(x)
      case None =>
        getImageLocation(elementId, size, image) match {
          case file if file.exists() => Some((file, size))
          case _ => None
        }
    })
  }

  def deleteImage(elementId: StringId, image: FileSlug): Boolean = {
    val folder = getContainerFolder(elementId, image)
    if(!folder.exists()) false else {
      FileUtils.forceDelete(folder)
      true
    }
  }

  private def sizesToImages(elementId: StringId, slug: FileSlug, sizes: Seq[SizeType]): Try[Seq[Image]] = {
    Try {
      sizes.map { size =>
        getImage(elementId, slug) match {
          case None => throw new RuntimeException("can't find image for that size")
          case Some(image) => image
        }
      }
    }
  }

  private def renameWithFileExtension(temp: File, target: FileSlug): File = {
    val tempBaseName = FilenameUtils.getBaseName(temp.getName)
    val newTempName = tempBaseName + "." + target.extension
    val newFullPath = FilenameUtils.concat(temp.getParentFile.getCanonicalPath, newTempName)
    val newTempFile = new File(newFullPath)
    if(!temp.getCanonicalPath.equals(newTempFile.getCanonicalPath)){
      FileUtils.moveFile(temp, newTempFile)
    }
    newTempFile
  }

  private def getPaletteLocation(elementId: StringId, imageSlug: FileSlug): File = {
    val folder = getContainerFolder(elementId, imageSlug)
    val fileName = "palette.txt"
    Paths.get(folder.getPath, fileName).toFile
  }

  private def getImageLocation(elementId: StringId, size: SizeType, imageSlug: FileSlug): File = {
    val folder = getContainerFolder(elementId, imageSlug)
    val fileName = imageSlug.withExtension(finalImageExtension).value
    Paths.get( folder.getPath, size.name, fileName).toFile
  }

  private def getUniqueFileSlug(elementId: StringId, fileName: FileSlug): FileSlug = {
    val potentialFile = getContainerFolder(elementId, fileName)
    if (!potentialFile.exists()) fileName else {
      val time = Instant.now().toEpochMilli
      val newFileName = fileName.withPrefix(time.toString)
      getUniqueFileSlug(elementId, newFileName)
    }
  }

  private def getContainerFolder(elementId: StringId, fileName: FileSlug): File = {
    Paths.get(
      getContainerRoot(elementId).toPath.toString,
      fileName.baseName
    ).toFile
  }

  private def getContainerRoot(elementId: StringId): File = {
    Paths.get(filesRoot, elementId.value, classification).toFile
  }

  private def getContainerFolders(elementId: StringId): Seq[File] = {
    val containerRoot = getContainerRoot(elementId)
    if(containerRoot.exists() && containerRoot.isDirectory) {
      containerRoot.listFiles().filter(_.isDirectory)
    } else {
      Seq.empty
    }
  }

  private def deleteImageDir(elementId: StringId, fileName: FileSlug): Unit = {
    val container = getContainerFolder(elementId, fileName)
    if (container.exists()) {
      FileUtils.deleteDirectory(container)
    }
  }

  private def processImageBySize(originalFile: File, elementId: StringId, fileName: FileSlug): Try[Seq[SizeType]] = {
    val originalFileSize = getSizeOfImage(originalFile)
    SizeType.sizes.filter( size => {
      originalFileSize > size.value || size == FullSize
    }).foldLeft(Success(Seq.empty) : Try[Seq[SizeType]])((acc, sizeType) => acc match {
      case Failure(_) => acc
      case Success(seq) =>
        val destination = getImageLocation(elementId, sizeType, fileName.withExtension(finalImageExtension))
        val destinationSize = originalFileSize.getDownScaled(sizeType.value)
        transformImage(originalFile, destination, sizeType, destinationSize) match {
          case Failure(thr) => Failure(thr)
          case Success(file) => Success(sizeType +: seq)
        }
    })
  }

  private def processImagePalette(elementId: StringId, fileName: FileSlug): Try[Palette] = {
    val imageFileOpt = getImageFileWithSuggestedSize(elementId, SmallSize, fileName)
    if(imageFileOpt.isEmpty) return Failure(new Exception("no image found"))
    val palette = getPaletteFromImage(imageFileOpt.get._1)
    if(palette.isFailure) return palette
    val destination = getPaletteLocation(elementId, fileName)
    Try(palette.get.saveToFile(destination)).flatMap(_ => palette)
  }

  private def getPalette(elementId: StringId, fileName: FileSlug): Try[Palette] = {
    Try {
      val location = getPaletteLocation(elementId, fileName)
      Palette.readFromFile(location)
    }
  }

  // magick <image> +dither -colors 5 -define histogram:unique-colors=true -format "%c" histogram:info:
  private def getPaletteFromImage(file: File): Try[Palette] = {
    Try{
      val op = new IMOperation
      op.addImage(file.getCanonicalPath)
      op.addRawArgs("+dither")
      op.colors(5)
      op.define("histogram:unique-colors=true")
      op.format("\"%c\"")
      op.addRawArgs("histogram:info:")
      val cmd = new ImageCommand("magick")
      val output = new ArrayListOutputConsumer()
      cmd.setOutputConsumer(output)
      cmd.run(op)
      val cmdOutput = output.getOutput
      getHistogramInfoColors(cmdOutput)
    }
  }

  private def getHistogramInfoColors(array: java.util.ArrayList[String]): Palette = {
    val colors = array.asScala.map(line => {
      val rgbString = StringUtils.substringBetween(line," (", ") ")
      Color.fromCommaSeparated(rgbString)
    }).toList
    Palette(colors)
  }

  private def getSizeOfImage(file: File): ImageSize = {
    val op = new IMOperation
    op.addImage(file.getCanonicalPath)
    op.ping()
    op.format("%wx%h")
    op.addRawArgs("info:")
    val cmd = new ImageCommand("magick")
    val output = new ArrayListOutputConsumer()
    cmd.setOutputConsumer(output)
    cmd.run(op)
    val cmdOutput = output.getOutput
    val line = cmdOutput.get(0)
    val arr = line.split("x")
    val width = arr(0).toInt
    val height = arr(1).toInt
    ImageSize(width, height)
  }

  private def transformImage(original: File, destination: File, sizeType: SizeType, size: ImageSize): Try[File] = {
    Try{
      val op = new IMOperation
      op.addImage(original.getCanonicalPath)
      op.resize(size.x, size.y)
      op.strip()
      op.interlace("Plane")
      op.quality(sizeType.quality.toDouble)
      op.colorspace("sRGB")
      op.depth(8)
      op.format("JPEG")
      if(sizeType.blur > 0) {
        op.blur(sizeType.blur.toDouble)
      }
      op.addImage(destination.getCanonicalPath)

      destination.getParentFile.mkdirs()
      val cmd = new ImageCommand("magick")
      cmd.run(op)
      destination
    }
  }

  protected val classification: String
  protected val finalImageExtension: String = "jpeg"
  protected val filesRoot: String = cfg.get[String]("database.filesBaseLocation")
}

class PostImagesDB()(implicit cfg: Configuration) extends ImageFileDB {
  override val classification: String = "post_images"
}

class PostAttachmentDB()(implicit cfg: Configuration) extends AttachmentFileDB {
  protected val classification: String = "post_attachments"
}

class SiteImagesDB()(implicit cfg: Configuration) extends ImageFileDB {
  override val classification: String = "site_images"
}

class FileSystemInterface()(implicit ofg: Configuration) {
  val postAttachment = new PostAttachmentDB()
  val postImage = new PostImagesDB()
  val siteImage = new SiteImagesDB()
}

object FileSystemInterface {
  private var _fileSystemInterface: FileSystemInterface = _
  private var _cfg: Configuration = _

  def get(implicit  cfg: Configuration): FileSystemInterface = {
    if(_fileSystemInterface == null || !cfg.equals(_cfg)) {
      _cfg = cfg
      _fileSystemInterface = new FileSystemInterface()
    }
    _fileSystemInterface
  }
}
