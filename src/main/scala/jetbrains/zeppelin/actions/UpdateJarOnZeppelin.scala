package jetbrains.zeppelin.actions

import java.util.concurrent.Executors

import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.components.ZeppelinConnection
import jetbrains.zeppelin.service.SbtService

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class UpdateJarOnZeppelin extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service

    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    val f = Future {
      val projectPath = event.getProject.getBasePath
      val jarFile = SbtService.packageToJarCurrentProject(projectPath)
      zeppelinService.updateJar(jarFile)
    }

    f.onComplete { result =>
      if (result.isSuccess) {
        Notifications.Bus
          .notify(new Notification("Zeppelin Remote Run", " Zeppelin Remote Run:", "Jar was updated", NotificationType
            .INFORMATION))

      }
      if (result.isFailure) {
        Notifications.Bus
          .notify(new Notification("Zeppelin Remote Run", " Zeppelin Remote Run:", "Error during update jar", NotificationType
            .ERROR))
      }
    }
  }
}