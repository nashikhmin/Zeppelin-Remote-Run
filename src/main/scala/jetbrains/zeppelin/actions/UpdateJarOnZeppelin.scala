package jetbrains.zeppelin.actions

import java.util.concurrent.Executors

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.components.ZeppelinConnection
import jetbrains.zeppelin.service.SbtService

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class UpdateJarOnZeppelin extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinConnection.connectionFor(event.getProject)
    val zeppelinService = connection.service

    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    connection.printMessage("Start update jar...")
    val f = Future {
      val projectPath = event.getProject.getBasePath
      val jarFile = SbtService.packageToJarCurrentProject(projectPath)
      zeppelinService.updateJar(jarFile)
    }

    f.onComplete { result =>
      if (result.isSuccess) {
        connection.printMessage("Jar file is updated")
      }
      if (result.isFailure) {
        connection.printError("Jar update is failed")
      }
    }

  }
}