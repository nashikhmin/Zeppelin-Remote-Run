package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.atomic.AtomicBoolean

import com.intellij.openapi.progress._
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.service.{DefaultTableOutputHandler, TableOutputHandler}
import org.intellij.plugin.zeppelin.utils.{ThreadRun, ZeppelinLogger}

class GuiExecutionHandler(project: Project) extends ExecutionHandler {
  private val isCompletedValue: AtomicBoolean = new AtomicBoolean(false)
  private var executionProgress: ExecutionProgress = ExecutionProgress()
  init()

  def isCompleted: AtomicBoolean = isCompletedValue

  case class ExecutionProgress(status: String = "", percentage: Double = 0)

  override def onError(result: ExecutionResults = ExecutionResults()): Unit = {
    ZeppelinLogger.printError(ZeppelinConstants.PARAGRAPH_ERROR)
    result.msg.foreach(it => ZeppelinLogger.printError(it.data))
    isCompletedValue.set(true)
  }

  override def onOutput(result: OutputResponse, isAppend: Boolean): Unit = {
    if (result.data.isEmpty) return
    ZeppelinLogger.printMessage(result.data)
  }

  override def onProgress(percentage: Double): Unit = {
    executionProgress = executionProgress.copy(percentage = percentage)
  }

  override def onSuccess(executionResults: ExecutionResults = ExecutionResults()): Unit = {
    executionResults.msg.foreach(it => {
      it.resultType match {
        case "TABLE" => {
          val handler = TableOutputHandler.getAll.headOption.getOrElse(DefaultTableOutputHandler)
          handler.invoke(project, it.data)
        }
        case _ => Unit
      }
    })
    ZeppelinLogger.printMessage(ZeppelinConstants.PARAGRAPH_COMPLETED)
    isCompletedValue.set(true)
  }

  override def onUpdateExecutionStatus(status: String): Unit = {
    executionProgress = executionProgress.copy(status = status)
  }

  private def init(): Unit = {
    val manager = ProgressManager.getInstance
    val task = new Task.Backgroundable(project, "Zeppelin Run", false) {
      override def run(progressIndicator: ProgressIndicator): Unit = {
        progressIndicator.setText("Run paragraph on Zeppelin...")
        while (!isCompletedValue.get()) {
          progressIndicator.setText2(executionProgress.status)
          progressIndicator.setFraction(executionProgress.percentage)
          Thread.sleep(200)
        }
      }
    }
    manager.runProcessWithProgressAsynchronously(task, ThreadRun.createBgIndicator(project, "Zeppelin execution"))
  }

}

object GuiExecutionHandler {
  def apply(project: Project): GuiExecutionHandler = new GuiExecutionHandler(project)
}

class GuiExecutionHandlerFactory(project: Project) extends ExecutionHandlerFactory {
  def create(): ExecutionHandler = GuiExecutionHandler(project)
}

object GuiExecutionHandlerFactory {
  def apply(project: Project): GuiExecutionHandlerFactory = new GuiExecutionHandlerFactory(project)
}