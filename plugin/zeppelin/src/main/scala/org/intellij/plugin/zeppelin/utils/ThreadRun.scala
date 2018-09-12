package org.intellij.plugin.zeppelin.utils

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.intellij.openapi.application.{Application, ApplicationManager}
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressIndicatorProvider, ProgressManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.{Computable, ThrowableComputable}
import com.intellij.util.Processor

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.control.Exception.catching
import scala.util.{Failure, Success, Try}

object ThreadRun {
  implicit def toCallable[T](action: => T): Callable[T] = () => action

  import scala.language.implicitConversions

  implicit def toComputable[T](action: => T): Computable[T] = () => action

  implicit def toIdeaFunction[A, B](f: Function[A, B]): com.intellij.util.Function[A, B] = (param: A) => f(param)

  implicit def toProcessor[T](action: T => Boolean): Processor[T] = (t: T) => action(t)

  implicit def toRunnable(action: => Any): Runnable = () => action

  private val DEFAULT_MAX_EXECUTION_TIME = 120

  def createBgIndicator(project: Project, name: String): ProgressIndicator = {
    Option(ProgressIndicatorProvider.getGlobalProgressIndicator).getOrElse(
      new BackgroundableProcessIndicator(
        project, name, PerformInBackgroundOption.ALWAYS_BACKGROUND, null, null, false
      )
    )
  }

  def inReadAction[T](body: => T): T = {
    ApplicationManager.getApplication match {
      case application if application.isReadAccessAllowed => body
      case application => application.runReadAction(body)
    }
  }

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

  def withProgressSynchronously[T](title: String)(body: => T): T = {
    withProgressSynchronouslyTry[T](title)(_ => body) match {
      case Success(result) => result
      case Failure(exception) => throw exception
    }
  }

  def withProgressSynchronouslyTry[T](title: String)(body: ProgressManager => T): Try[T] = {
    val manager = ProgressManager.getInstance
    catching(classOf[Exception]).withTry {
      manager.runProcessWithProgressSynchronously(new ThrowableComputable[T, Exception] {
        def compute: T = body(manager)
      }, title, false, null)
    }
  }
}