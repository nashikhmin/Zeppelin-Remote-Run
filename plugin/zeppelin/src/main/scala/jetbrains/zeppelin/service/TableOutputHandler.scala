package jetbrains.zeppelin.service

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.utils.ZeppelinLogger

trait TableOutputHandler {
  def invoke(project: Project, msg: String)
}

object TableOutputHandler {
  val Id = "org.jetbrains.scala.zeppelin.tableOutputHandler"
  val EP_NAME: ExtensionPointName[TableOutputHandler] =
    ExtensionPointName.create[TableOutputHandler](Id)

  def getAll: Array[TableOutputHandler] = EP_NAME.getExtensions
}


object DefaultTableOutputHandler extends TableOutputHandler {
  override def invoke(project: Project, msg: String): Unit = ZeppelinLogger.printMessage(msg)
}