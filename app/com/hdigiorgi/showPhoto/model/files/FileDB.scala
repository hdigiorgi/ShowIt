package com.hdigiorgi.showPhoto.model.files

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.Instant

import com.hdigiorgi.showPhoto.model.{FileSlug, Slug, StringId}
import org.apache.commons.io.FileUtils
import play.api.Configuration

import scala.annotation.tailrec
import scala.util.control.TailCalls.TailRec

case class SizeType(value: String)
object SizeType {
  val Original = SizeType("original")
  val Medium = SizeType("medium")
  val Small = SizeType("small")
}

trait FileInterface {
  def getNewLocation(container: StringId, fileName: String): Path
  def getFiles(container: StringId): Seq[File]
}

class GenericFileDB()(implicit private val cfg: Configuration){

  final def getFile(elementId: StringId, fileName: FileSlug): File = getFile(elementId, SizeType.Original, fileName)

  def getFile(elementId: StringId, size: SizeType, fileName: FileSlug): File = {
    val folder = getContainerFolder(elementId, fileName)
    Paths.get( folder.getPath, size.value, fileName.value).toFile
  }

  def getNewFile(elementId: StringId, fileName: FileSlug): File = {
    val potentialFile = getFile(elementId, fileName)
    if (!potentialFile.exists()) potentialFile else {
      val time = Instant.now().toEpochMilli
      val newFileName = FileSlug(f"${time}_$fileName")
      getNewFile(elementId, newFileName)
    }
  }

  def getContainerFolder(elementId: StringId, fileName: FileSlug): File = {
    Paths.get(
      getContainerRoot(elementId).toPath.toString,
      fileName.value
    ).toFile
  }

  def getContainerRoot(elementId: StringId): File = {
    Paths.get(filesRoot, elementId.value, classification).toFile
  }

  def getContainerFolders(elementId: StringId): Seq[File] = {
    val containerRoot = getContainerRoot(elementId)
    if(containerRoot.exists() && containerRoot.isDirectory) {
      containerRoot.listFiles().filter(_.isDirectory)
    } else {
      Seq.empty
    }
  }

  def getStoredFileNames(elementId: StringId): Seq[String] = {
    getContainerFolders(elementId).map(_.getName)
  }

  def move(origin: File, destination: File): File = {
    FileUtils.forceMkdirParent(destination)
    FileUtils.moveFile(origin, destination)
    destination
  }

  def moveEnsureNew(origin: File, elementId: StringId, fileName: FileSlug): File = {
    val destination = getNewFile(elementId, fileName)
    move(origin, destination)
  }

  protected val classification: String = "generic_files"
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
