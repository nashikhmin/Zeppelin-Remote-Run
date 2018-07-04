package jetbrains.zeppelin.service

import java.util.concurrent.Executors

import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.api.{User, ZeppelinConnectionException, ZeppelinLoginException}
import jetbrains.zeppelin.utils.ZeppelinLogger

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/**
  * Main class that implement logic of communication with Zeppelin
  */
class ZeppelinActionService(address: String, port: Int, user: Option[User]) {
  var zeppelinService: ZeppelinAPIService = ZeppelinAPIService(address, port, user)

  /**
    * Run code on the Zeppelin server
    *
    * @param code         - code, that be executed
    * @param notebookName - name of notebook where te paragraph will be created
    */
  def runCode(code: String, notebookName: String): Unit = {
    if (!connectIfNotYet()) return

    ZeppelinLogger.printMessage(s"Run paragraph with text: $code")
    val handler = new OutputHandler {
      override def onError(): Unit = {
        ZeppelinLogger.printError("Paragraph Run Error")
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        if (result.data.isEmpty) {
          return
        }
        ZeppelinLogger.printMessage(result.data)
      }

      override def onSuccess(): Unit = {
        ZeppelinLogger.printMessage("Paragraph is completed")
      }
    }
    zeppelinService.runCode(code, handler, notebookName)
  }

  /**
    * Test the connection and try to connect if the server is disconnected
    *
    * @return the server is connected
    */
  private def connectIfNotYet(): Boolean = {
    if (zeppelinService.isConnected) {
      return true
    }
    try {
      zeppelinService.close()
      zeppelinService = ZeppelinAPIService(address, port, user)
      zeppelinService.connect(false)
    }
    catch {
      case _: ZeppelinConnectionException => {
        ZeppelinLogger
          .printError(s"Connection error. Check that $address:$port is available")
      }
      case _: ZeppelinLoginException => ZeppelinLogger.printError(s"Authentication error. Check login and password")
    }
    zeppelinService.isConnected
  }

  /**
    * Upload the current project to the Zeppelin server as a jar file
    *
    * @param projectPath - full path to the project
    */
  def updateJar(projectPath: String): Unit = {
    if (!connectIfNotYet()) return

    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    ZeppelinLogger.printMessage("Start update jar...")
    val f = Future {

      val jarFile = SbtService().packageToJarCurrentProject(projectPath)
      zeppelinService.updateJar(jarFile)
    }

    f.onComplete {
      result => {
        if (result.isSuccess) {
          ZeppelinLogger.printSuccess("Jar file is updated")
        }
        if (result.isFailure) {
          ZeppelinLogger.printError("Jar update is failed")
        }
      }
    }
  }

  /**
    * Method that close all connections and free resources
    */
  def destroy(): Unit = {
    zeppelinService.close()
  }
}

object ZeppelinActionService {
  def apply(address: String, port: Int, user: Option[User]): ZeppelinActionService = {
    new ZeppelinActionService(address, port, user)
  }
}
