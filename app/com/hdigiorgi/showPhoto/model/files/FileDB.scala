package com.hdigiorgi.showPhoto.model.files

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.Instant

import com.hdigiorgi.showPhoto.model.{FileSlug, Slug, StringId}
import javax.imageio.{ImageIO, ImageWriteParam}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.im4java.core._
import org.im4java.process.ArrayListOutputConsumer
import play.api.Configuration


case class SizeType(name: String, value: Integer, quality: Integer) {
  def >(that: SizeType): Boolean = this.value > that.value
  def <(that: SizeType): Boolean = this.value < that.value
}
object SizeType {
  val sizes = Seq(FullSize, MediumSize, SmallSize)
}
object FullSize extends SizeType("full", 2560, 50)
object MediumSize extends SizeType("medium", 1280, 40)
object SmallSize extends SizeType("small", 640, 30)

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

object GenericFileDB {
  type ProcessingResult = Either[Exception, Map[SizeType, File]]
}

class GenericFileDB()(implicit private val cfg: Configuration){
  import GenericFileDB._

  def getStoredImageIds(elementId: StringId): Seq[String] = {
    getContainerFolders(elementId).map(_.getName)
  }

  def process(tempInputFile: File, elementId: StringId, inFileName: FileSlug): ProcessingResult = {
    val toProcess = renameWithFileExtension(tempInputFile, inFileName)
    val destinationSlug = getUniqueFileSlug(elementId, inFileName)
    val processResult = processImageBySize(toProcess, elementId, destinationSlug)
    FileUtils.forceDelete(toProcess)
    if(processResult.isLeft){
      deleteImageDir(elementId, destinationSlug)
    }
    processResult
  }

  def getImageId(result: ProcessingResult): Option[String] = result match {
    case Left(_) => None
    case Right(map) => Some(map.head._2.getName)
  }

  def getImageWithSuggestedSize(elementId: StringId, size: SizeType, image: FileSlug): Option[File] = {
    val targetFile = getImageLocation(elementId, size, image)
    if(targetFile.exists()) return Some(targetFile)

    val options = SizeType.sizes.filter(_> size).reverse ++ SizeType.sizes.filter(_< size)
    options.foldLeft(None: Option[File])((acc, size) => acc match {
      case Some(x) => Some(x)
      case None =>
        getImageLocation(elementId, size, image) match {
          case file if file.exists() => Some(file)
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

  private def renameWithFileExtension(temp: File, target: FileSlug): File = {
    val tempBaseName = FilenameUtils.getBaseName(temp.getName)
    val newTempName = tempBaseName + "." + target.extension
    val newFullPath = FilenameUtils.concat(temp.getParentFile.getCanonicalPath, newTempName)
    val newTempFile = new File(newFullPath)
    FileUtils.moveFile(temp, newTempFile)
    newTempFile
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

  private def processImageBySize(originalFile: File, elementId: StringId, fileName: FileSlug): ProcessingResult = {
    val originalFileSize = getSizeOfImage(originalFile)
    SizeType.sizes.filter( size => {
      originalFileSize > size.value || size == FullSize
    }).foldLeft(Right(Map.empty) : ProcessingResult)((acc, sizeType) => acc match {
      case Left(_) => acc
      case Right(map) =>
        val destination = getImageLocation(elementId, sizeType, fileName.withExtension(finalImageExtension))
        val destinationSize = originalFileSize.getDownScaled(sizeType.value)
        transformImage(originalFile, destination, sizeType.quality, destinationSize) match {
          case Left(e) => Left(e)
          case Right(file) => Right(map + (sizeType -> file))
        }
    })
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

  private def transformImage(original: File, destination: File, quality: Int, size: ImageSize): Either[Exception, File] = {
    try {
      val op = new IMOperation
      op.addImage(original.getCanonicalPath)
      op.resize(size.x, size.y)
      op.strip()
      op.interlace("Plane")
      op.quality(quality.toDouble)
      op.colorspace("sRGB")
      op.depth(8)
      op.format("JPEG")
      op.addImage(destination.getCanonicalPath)

      destination.getParentFile.mkdirs()
      val cmd = new ImageCommand("magick")
      cmd.run(op)
      Right(destination)
    } catch {
      case e: Exception => Left(e)
    }
  }

  protected val classification: String = "images"
  protected val finalImageExtension: String = "jpeg"
  protected val filesRoot: String = cfg.get[String]("database.filesLocation")
}

class ImageFileDB()(implicit private val cfg: Configuration) extends GenericFileDB {
  override val classification = "images"
}

class AttachmentFileDB()(implicit private val cfg: Configuration) extends GenericFileDB {
  override val classification = "attachments"
}

class FileSystemInterface()(implicit ofg: Configuration) {
  val attachment = new AttachmentFileDB()
  val image = new ImageFileDB()
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
