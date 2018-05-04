package jetbrains.zeppelin.utils

import java.util.concurrent.{Executors, TimeUnit}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object ThreadRun {
  private val DEFAULT_MAX_EXECUTION_TIME = 120

  def runWithTimeout[T](f: => T): Unit = {
    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    Await.result(Future(f), Duration(DEFAULT_MAX_EXECUTION_TIME, TimeUnit.SECONDS))
  }
}