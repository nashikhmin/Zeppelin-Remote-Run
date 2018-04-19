package jetbrains.zeppelin.api

import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.service.ZeppelinService
import org.scalatest.{FunSuite, Matchers}

class ZeppelinAPITest extends FunSuite with Matchers {
  private val monitor = AnyRef
  private val login = "user1"
  private val password = "password2"
  private val url = "localhost"
  private val port = 8080
  private val folder = "TestRemoteNotebooks/"
  private val notebookName = s"${folder}goldenCase"

  test("Zeppelin.CreateNotebookAndRunParagraph") {

    val zeppelinService = new ZeppelinService(url, port, notebookName)
    zeppelinService.connect(login, password)

    val code = "println(\"hello world\")"
    var waitResult = true
    var result = "none"
    val handler = new OutputHandler {
      override def onError(): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "fail"
          monitor.notifyAll()
        }
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        assert(result.data.isEmpty || result.data == "hello world\n")
      }

      override def onSuccess(): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "success"
          monitor.notifyAll()
        }
      }
    }

    zeppelinService.runCode(code, handler)
    monitor.synchronized {
      while (waitResult) {
        monitor.wait(20 * 1000)
      }
    }
    assert(result == "success")
  }

  test("Zeppelin.GetNotebooks") {
    val zeppelinRestApi = new ZeppelinRestApi(url, port)
    zeppelinRestApi.login(login, password)
    val notes = zeppelinRestApi.getNotes(folder)
    zeppelinRestApi.createNotebook(NewNotebook(s"${folder}testAdd"))
    val notesAfterAdd = zeppelinRestApi.getNotes(folder)
    assert(notesAfterAdd.length - notes.length == 1)
  }

  test("Zeppelin.UploadJar") {
    val zeppelinService = new ZeppelinService(url, port, notebookName)
    zeppelinService.connect(login, password)
    val interpreterWithoutDependencies = zeppelinService.interpreter.copy(dependencies = List.empty)
    zeppelinService.zeppelinRestApi.updateInterpreterSettings(interpreterWithoutDependencies)

    assert(zeppelinService.interpreter == interpreterWithoutDependencies)

    val jarPath = "/home/nashikhmin/git/zeppelin-remote/target/scala-2.12/zeppelin-remote_2.12-0.1.jar"
    val interpreterWithDependency = interpreterWithoutDependencies.copy(dependencies = List(Dependency(jarPath)))
    zeppelinService.updateJar(jarPath)

    assert(zeppelinService.interpreter == interpreterWithDependency)
  }
}
