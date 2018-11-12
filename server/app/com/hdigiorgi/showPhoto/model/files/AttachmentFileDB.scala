package com.hdigiorgi.showPhoto.model.files

import java.io.File
import java.nio.file.Paths
import java.text.DecimalFormat

import com.hdigiorgi.showPhoto.model.StringId
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.{FileHeader, ZipParameters}
import net.lingala.zip4j.util.Zip4jConstants
import org.apache.logging.log4j.LogManager

import collection.JavaConverters._
import play.api.Configuration

import scala.util.{Failure, Try}

case class FileEntry(name: String, size: Long) {
  def sizeString: String = {
    if(size <= 0) return "0";
    val units = Seq("B", "kB", "MB", "GB", "TB")
    val digitGroups: Integer = (Math.log10(size)/Math.log10(1024)).toInt
    new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups.toDouble)) + " " + units(digitGroups)
  }
}
object FileEntry{
  def apply(fh: FileHeader): FileEntry = {
    new FileEntry(fh.getFileName, fh.getUncompressedSize)
  }
}

abstract class AttachmentFileDB()(implicit private val cfg: Configuration) {

  def addFile(id: StringId, inputFile: File, inputFileName: String): Try[FileEntry] = Try {
    val toCompress = renameFile(inputFile, inputFileName)
    zip(id).addFile(toCompress, compressionParams)
    FileEntry(inputFileName, inputFile.length())
  }

  def removeFile(id: StringId, name: String): Try[FileEntry] = Try{
    zip(id).removeFile(name)
    FileEntry(name, 0)
  }

  def tryListFiles(id: StringId): Try[Seq[FileEntry]] = Try {
    optZip(id).map{_.getFileHeaders.asScala.map{ header =>
      val fileHeader = header.asInstanceOf[FileHeader]
      FileEntry(fileHeader.getFileName, fileHeader.getUncompressedSize)
    }}.getOrElse(Seq())
  }

  def listFiles(id: StringId): Seq[FileEntry] = tryListFiles(id).recoverWith{
    case ex: Exception =>
      logger.error(ex)
      Failure(ex)
  }.getOrElse(Seq.empty)

  def location: String = filesRoot

  private val compressionParams = {
    val zipParameters = new ZipParameters()
    zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE)
    zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA)
    zipParameters
  }

  private def renameFile(file: File, name: String): File = {
    val containingFolder = file.getParentFile.getCanonicalPath
    val destinationFile = Paths.get(containingFolder, name).toFile
    file.renameTo(destinationFile)
    destinationFile
  }

  private def zip(id: StringId): ZipFile = {
    val zipLocation = getZipLocation(id)
    if(!zipLocation.exists()) {
      zipLocation.getParentFile.mkdir()
    }
    new ZipFile(zipLocation)
  }

  private def optZip(id: StringId): Option[ZipFile] = {
    val z = zip(id)
    if(z.getFile.exists()) Some(z) else None
  }

  private def getZipLocation(id: StringId): File = {
    Paths.get(containerRoot(id).getPath, zipFileName).toFile
  }

  private def containerRoot(elementId: StringId): File = {
    Paths.get(filesRoot, subFolder, elementId.value, classification).toFile
  }

  protected val subFolder: String
  protected val classification: String = "attachments"
  protected val zipFileName: String = "attachment.zip"
  protected val filesRoot: String = cfg.get[String]("database.filesBaseLocation")
  protected val logger = LogManager.getLogger(this.getClass)
}