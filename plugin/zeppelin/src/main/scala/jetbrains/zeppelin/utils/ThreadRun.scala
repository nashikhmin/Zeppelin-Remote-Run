package jetbrains.zeppelin.utils

import java.util.concurrent.{Executors, TimeUnit}

import com.intellij.openapi.application.{Application, ApplicationManager}
import com.intellij.openapi.util.Computable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object ThreadRun {
  private val DEFAULT_MAX_EXECUTION_TIME = 120

  def inWriteAction[T](body: => T): T = {
    val application: Application = ApplicationManager.getApplication

    if (application.isWriteAccessAllowed) {
      body
    } else {
      application.runWriteAction(
        new Computable[T] {
          def compute: T = body
        }
      )
    }
  }

  def invokeLater[T](body: => T): Unit = {
    ApplicationManager.getApplication.invokeLater(() => body)
  }

  def runWithTimeout[T](f: => T): Unit = {
    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    Await.result(Future(f), Duration(DEFAULT_MAX_EXECUTION_TIME, TimeUnit.SECONDS))
  }
}