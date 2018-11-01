package controllers

import java.io.File
import akka.stream.{KillSwitches, SharedKillSwitch}
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}
import scala.collection.mutable

object DownloadHelper {

  def stopDownloads(files: Seq[File]): Unit = {
    val ids = files.map(getIdFromFile)
    BlockedFilesContainer.addBlocked(ids)
    KillSwitchContainer.kill(ids)
  }

  def allowDownloadsAgain(files: Seq[File]): Unit = {
    BlockedFilesContainer.removeBlocked(files.map(getIdFromFile))
  }

  def getInlineResult(file: File): Result = {
    getGenericFileResult(file, inlineHeader)
  }

  def getAttachmentResult(file: File): Result = {
    getGenericFileResult(file, attachmentHeader)
  }

  private def getGenericFileResult(file: File, headerMap: String => Map[String, String]): Result = {
    val fileId = getIdFromFile(file)
    BlockedFilesContainer.throwIfBlocked(fileId, DownloadKilledManuallyBeforeStarts)
    val fileName = file.getName
    val path = file.toPath
    val size = Option(file.length())
    val contentType = Option(java.nio.file.Files.probeContentType(path))

    val source: Source[ByteString, _] = FileIO.fromPath(path)
    val killSwitchedSource = KillSwitchContainer.withKillSwitch(fileId, source)

    val header = ResponseHeader(200, headerMap(fileName))
    val body = HttpEntity.Streamed(killSwitchedSource, size, contentType)

    Result(
      header = header,
      body = body
    )
  }

  private def getIdFromFile(file: File): String = file.getCanonicalPath

  private def downloadHeader(display: String, fileName: String): Map[String, String] = {
    Map("Content-Disposition" -> f"${display}; filename=${fileName};")
  }
  private def inlineHeader(fileName: String) = downloadHeader("inline", fileName)

  private def attachmentHeader(fileName: String) = downloadHeader("attachment", fileName)

  private object BlockedFilesContainer {
    private var list = mutable.ListBuffer[String]()

    def isBlocked(id: String): Boolean = this.synchronized{
      list.contains(id)
    }
    def addBlocked(ids: Seq[String]): Unit = this.synchronized{
      ids.foreach{ list += _ }
    }
    def removeBlocked(ids: Seq[String]): Unit =  this.synchronized{
      ids.foreach{ list -= _ }
    }
    def throwIfBlocked(id: String, ex: Throwable): Unit = {
      if(isBlocked(id)) {
        throw ex
      }
    }
  }

  private object KillSwitchContainer {
    private val map = mutable.HashMap[String, (SharedKillSwitch, Integer)]()

    def withKillSwitch[A,B](id: String, source: Source[A,B]): Source[A,B] = {
      val killSwitch = getKillSwitch(id)
      val source2 = source.via(killSwitch.flow)
      source2.watchTermination(){(mat,_) =>
        maybeRemoveKillSwitch(id)
        mat
      }
    }

    private def maybeRemoveKillSwitch(id: String): Boolean = this.synchronized{
      map.get(id) match {
        case None => true
        case Some((ks, count)) if count > 1 =>
          map.update(id, (ks, count - 1))
          false
        case Some((_, count)) if count <= 1 => {
          map.remove(id)
          true
        }
      }
    }

    def getKillSwitch(id: String): SharedKillSwitch = {
      this.synchronized{
        val (killSwitch, count) = map.getOrElseUpdate(id, (KillSwitches.shared(id), 0))
        map.update(id, (killSwitch, count + 1))
        killSwitch
      }
    }

    def kill(id: String): Unit = kill(Seq(id))
    def kill(ids: Seq[String]): Unit = this.synchronized{ ids.foreach{ id =>
      map.get(id) match {
        case None => None
        case Some((killSwitch, _)) =>
          killSwitch.abort(DownloadKilledManuallyWhileDownloading)
      }
      map.remove(id)
    }}
  }

  case class DownloadKilledManuallyByServer(private val message: String = "", private val cause: Throwable = None.orNull)
    extends Exception(message, cause)
  object DownloadKilledManuallyBeforeStarts extends DownloadKilledManuallyByServer("before_starts")
  object DownloadKilledManuallyWhileDownloading extends DownloadKilledManuallyByServer("while_downloading")
}