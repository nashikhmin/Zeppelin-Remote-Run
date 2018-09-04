package org.intellij.plugin.zeppelin

import java.util.concurrent.Executors

import org.scalatest.{FunSuite, Matchers}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
abstract class AbstractScalaTest extends FunSuite with Matchers {
  private val DEFAULT_MAX_EXECUTION_TIME = 5

  def runWithTimeout[T](f: => T): Option[T] = {
    //    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    //    Await.result(Future(f), Duration(DEFAULT_MAX_EXECUTION_TIME, TimeUnit.SECONDS)).asInstanceOf[Option[T]]
    None
  }
}