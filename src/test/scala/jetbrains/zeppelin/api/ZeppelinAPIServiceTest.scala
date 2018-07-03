package jetbrains.zeppelin.api

import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.service.ZeppelinAPIService

class ZeppelinAPIServiceTest extends AbstractScalaTest {
  private val monitor = AnyRef
  private val login = "admin"
  private val password = "password1"
  private val url = "localhost"
  private val port = 8080
  private val folder = "TestRemoteNotebooks/"
  private val notebookName = s"${folder}goldenCase"


  test("Zeppelin.RunWihWrongAddress") {
    val zeppelinService = ZeppelinAPIService(url, port + 666, Some(User(login, password)))
    assertThrows[ZeppelinConnectionException](zeppelinService.connect())
    assert(!zeppelinService.isConnected)
  }

  test("Zeppelin.RunWihWrongLogin") {
    val zeppelinService = ZeppelinAPIService(url, port, Some(User(login + "wrong", password)))
    assertThrows[ZeppelinLoginException](zeppelinService.connect())
    assert(!zeppelinService.isConnected)
  }

  test("Zeppelin.CreateNotebookAndRunParagraphWithLogin") {
    val zeppelinService = ZeppelinAPIService(url, port, Some(User(login, password)))
    performSimpleExecuteTest(zeppelinService)
  }


  test("Zeppelin.CreateNotebookAndRunParagraphWithoutLogin") {
    val zeppelinService = ZeppelinAPIService(url, port, None)
    performSimpleExecuteTest(zeppelinService)
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
    val zeppelinService = ZeppelinAPIService(url, port, Some(User(login, password)))
    zeppelinService.connect()
    val interpreterWithoutDependencies = zeppelinService.interpreter.copy(dependencies = List.empty)
    zeppelinService.updateInterpreterSetting(interpreterWithoutDependencies)

    assert(zeppelinService.interpreter == interpreterWithoutDependencies)

    val jarPath = getClass.getResource("/scala_example.jar").getPath
    val interpreterWithDependency = interpreterWithoutDependencies.copy(dependencies = List(Dependency(jarPath)))

    zeppelinService.updateJar(jarPath)

    assert(zeppelinService.interpreter == interpreterWithDependency)
    assert(zeppelinService.interpreter.status == InterpreterStatus.READY)
  }

  def performSimpleExecuteTest(zeppelinService: ZeppelinAPIService): Unit = {
    zeppelinService.connect()
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
}
