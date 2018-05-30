package jetbrains.zeppelin.api

import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.service.ZeppelinService

class ZeppelinAPITest extends AbstractScalaTest {
  private val monitor = AnyRef
  private val login = "user1"
  private val password = "password2"
  private val url = "localhost"
  private val port = 8080
  private val folder = "TestRemoteNotebooks/"
  private val notebookName = s"${folder}goldenCase"

  test("Zeppelin.RunWihWrongAddress") {
    val zeppelinService = ZeppelinService(url, 666)
    assertThrows[ZeppelinConnectionException](zeppelinService.connect(login, password))
  }

  test("Zeppelin.RunWihWrongLogin") {
    val zeppelinService = ZeppelinService(url, port)
    assertThrows[ZeppelinLoginException](zeppelinService.connect(login + "wrong", password))
  }

  test("Zeppelin.CreateNotebookAndRunParagraph") {
    val zeppelinService = ZeppelinService(url, port)
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

    zeppelinService.runCode(code, handler, notebookName)
    monitor.synchronized {
      while (waitResult) {
        monitor.wait(20 * 1000)
      }
    }
    assert(result == "success")
  }

  test("Zeppelin.GetNotebooks") {
    val zeppelinRestApi = ZeppelinRestApi(url, port)
    zeppelinRestApi.login(login, password)
    val notes = zeppelinRestApi.getNotebooks(folder)
    zeppelinRestApi.createNotebook(NewNotebook(s"${folder}testAdd"))
    val notesAfterAdd = zeppelinRestApi.getNotebooks(folder)
    assert(notesAfterAdd.length - notes.length == 1)
  }

  test("Zeppelin.UploadJar") {

    val zeppelinService = ZeppelinService(url, port)
    zeppelinService.connect(login, password)
    val interpreterWithoutDependencies = zeppelinService.interpreter.copy(dependencies = List.empty)
    zeppelinService.updateInterpreterSetting(interpreterWithoutDependencies)

    assert(zeppelinService.interpreter == interpreterWithoutDependencies)

    val jarPath = getClass.getResource("/scala_example.jar").getPath
    val interpreterWithDependency = interpreterWithoutDependencies.copy(dependencies = List(Dependency(jarPath)))

    zeppelinService.updateJar(jarPath)

    assert(zeppelinService.interpreter == interpreterWithDependency)
    assert(zeppelinService.interpreter.status == InterpreterStatus.READY)
  }
}
