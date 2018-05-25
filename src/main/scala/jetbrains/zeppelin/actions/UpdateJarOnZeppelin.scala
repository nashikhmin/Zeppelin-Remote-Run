package jetbrains.zeppelin.actions

import java.util.concurrent.Executors

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.components.ZeppelinConnection
import jetbrains.zeppelin.service.SbtService
import jetbrains.zeppelin.utils.ZeppelinLogger

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class UpdateJarOnZeppelin extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service

    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    //ZeppelinLogger.printMessage("Start update jar...")
    val f = Future {
      val projectPath = event.getProject.getBasePath
      val sbtService: SbtService = SbtService.apply
      val jarFile = sbtService.packageToJarCurrentProject(projectPath)
      zeppelinService.updateJar(jarFile)
    }

    f.onComplete { result =>
      if (result.isSuccess) {
        ZeppelinLogger.printMessage("Jar file is updated")
      }
      if (result.isFailure) {
        ZeppelinLogger.printError("Jar update is failed")
      }
    }
  }
}