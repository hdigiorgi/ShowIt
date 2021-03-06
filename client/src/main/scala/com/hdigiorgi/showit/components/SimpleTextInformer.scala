package com.hdigiorgi.showit.components
import org.scalajs.jquery.{JQuery, jQuery}
import com.hdigiorgi.showit.utils._

class SimpleTextInformer(id: String, parent: JQuery) extends ResultInformer {

  override def informError(msg: Option[String]): Unit = {
    val errorMsg = msg.getOrElse("")
    parent.empty()
    parent.append(errorElementString(errorMsg))
  }

  override def informWorking(_msg: Option[String]): Unit = {
    if(loadingElement.isEmpty) {
      parent.empty()
      parent.append(loadingElementString)
    }
  }

  override def informSuccess(msg: Option[String]): Unit = {
    parent.empty()
    parent.append(successElementString)
  }

  override def informHide(): Unit = {
    parent.empty()
  }

  private def loadingElement: Option[JQuery] = {
    val element = jQuery(f"#$loadingElementId")
    if(element.length <= 0) None else Some(element)
  }
  private def loadingElementId: String = f"$id-loading-element"
  private def loadingElementString: String = {
    f"<span class='simple-text-informer'><i id='$loadingElementId' class='fas fa-spinner fa-pulse'></i></span>"
  }
  private def successElementString: String = {
    f"<span class='simple-text-informer'><i id='$id-success-element' class='fas fa-check ok'></i></span>"
  }
  private def errorElementString(error: String): String = {
    f"<span id='$id-error-element' class='simple-text-informer badge badge-warning'>$error</span>"
  }

}

object SimpleTextInformer {
  def fromElement(element: JQuery): SimpleTextInformer = {
    val id = `!attr`(element, "id")
    new SimpleTextInformer(id, element)
  }
  def fromId(id: String): SimpleTextInformer = fromElement(`$`(id))
}