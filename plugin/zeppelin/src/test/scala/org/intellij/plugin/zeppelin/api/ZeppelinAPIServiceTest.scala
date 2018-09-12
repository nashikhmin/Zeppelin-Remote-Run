package org.intellij.plugin.zeppelin.api

import org.intellij.plugin.zeppelin.AbstractScalaTest
import org.intellij.plugin.zeppelin.models._
import org.intellij.plugin.zeppelin.service.execution.{ExecutionHandler, ExecutionResults, OutputResponse}

import scala.util.Random

class ZeppelinAPIServiceTest extends AbstractScalaTest {
  private val monitor = AnyRef
  private val login = "admin"
  private val password = "password1"
  private val url = "localhost"
  private val port = 8080
  private val folder = "TestRemoteNotebooks/"
  private val runCodeTestNotebook = s"${folder}runCode"
  private val interpreterIdTest = "md"
  private val interpreterSparkId = "spark"
  private val adminUser = Some(User(login, password))

  test("Zeppelin.RunWihWrongAddress") {
    val zeppelinService = ZeppelinApi(url, port + 666, adminUser)
    assertThrows[ZeppelinConnectionException](zeppelinService.connect())
    assert(!zeppelinService.isConnected)
  }

  test("Zeppelin.RunWihWrongLogin") {
    val zeppelinService = ZeppelinApi(url, port, Some(User(login + "wrong", password)))
    assertThrows[ZeppelinLoginException](zeppelinService.connect())
    assert(!zeppelinService.isConnected)
  }

  test("Zeppelin.CreateNotebookAndRunParagraphWithLogin") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    performSimpleExecuteTest(zeppelinService)
  }


  test("Zeppelin.CreateNotebookAndRunParagraphWithoutLogin") {
    val zeppelinService = ZeppelinApi(url, port, None)
    performSimpleExecuteTest(zeppelinService)
  }

  test("Zeppelin.CreateAndDeleteParagraphs") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    zeppelinService.connect()
    val originalNotebook = zeppelinService.getOrCreateNotebook(runCodeTestNotebook)
    val noteId = originalNotebook.id


    zeppelinService.deleteAllParagraphs(noteId)
    assert(zeppelinService.getOrCreateNotebook(runCodeTestNotebook).paragraphs.isEmpty)

    val paragraph = zeppelinService.zeppelinRestApi.createParagraph(noteId, "Hello")
    assert(zeppelinService.getOrCreateNotebook(runCodeTestNotebook).paragraphs.length == 1)

    zeppelinService.zeppelinRestApi.deleteParagraph(noteId, paragraph.id)
    assert(zeppelinService.getOrCreateNotebook(runCodeTestNotebook).paragraphs.isEmpty)
  }

  test("Zeppelin.GetNotebooks") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    zeppelinService.connect()

    val zeppelinRestApi = zeppelinService.zeppelinRestApi
    val notes = zeppelinRestApi.getNotebooks(folder)
    zeppelinRestApi.createNotebook(NewNotebook(s"${folder}testAdd"))
    val notesAfterAdd = zeppelinRestApi.getNotebooks(folder)
    assert(notesAfterAdd.length - notes.length == 1)

    zeppelinService.deleteAllNotebooksByPrefix(folder)
    assert(zeppelinRestApi.getNotebooks(folder).isEmpty)
  }

  test("Zeppelin.GetDefaultInterpreter") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    zeppelinService.connect()
    val notebook = zeppelinService.getOrCreateNotebook(runCodeTestNotebook)
    val interpreters = zeppelinService.allInterpreters.map(_.id)
    val rnd = new Random
    val randomInterpreter = interpreters(rnd.nextInt(interpreters.length))
    zeppelinService.setDefaultInterpreter(notebook.id, randomInterpreter)

    val result = zeppelinService.defaultInterpreter(notebook.id).get
    assert(result.id == randomInterpreter)
  }

  test("Zeppelin.InterpreterInstantiation") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    zeppelinService.connect()
    val interpreter = zeppelinService.interpreterById(interpreterIdTest)
    val globalInterpreter = interpreter.copy(option = InterpreterOption())
    zeppelinService.updateInterpreterSetting(globalInterpreter)
    assert(zeppelinService.interpreterById(interpreterIdTest) == globalInterpreter)

    val changedInterpreter = interpreter.copy(option = InterpreterOption(
      Some(InstantiationType.SHARED.toString),
      Some(InstantiationType.SCOPED.toString)))
    zeppelinService.updateInterpreterSetting(changedInterpreter)
    assert(zeppelinService.interpreterById(interpreterIdTest) == changedInterpreter)
  }

  test("Zeppelin.UploadJar") {
    val zeppelinService = ZeppelinApi(url, port, adminUser)
    zeppelinService.connect()

    val interpreterWithoutDependencies = zeppelinService.interpreterById(interpreterSparkId)
      .copy(dependencies = List.empty)
    zeppelinService.updateInterpreterSetting(interpreterWithoutDependencies)

    assert(zeppelinService.interpreterById(interpreterSparkId) == interpreterWithoutDependencies)

    val jarPath = getClass.getResource("/scala_example.jar").getPath
    val interpreterWithDependency = interpreterWithoutDependencies.copy(dependencies = List(Dependency(jarPath)))
    zeppelinService.updateInterpreterSetting(interpreterWithDependency)

    assert(zeppelinService.interpreterById(interpreterSparkId) == interpreterWithDependency)
    assert(zeppelinService.interpreterById(interpreterSparkId).status == InterpreterStatus.READY)
  }

  def performSimpleExecuteTest(zeppelinService: ZeppelinApi): Unit = {
    zeppelinService.connect()
    val notebook = zeppelinService.getOrCreateNotebook(runCodeTestNotebook)
    zeppelinService.setDefaultInterpreter(notebook.id, interpreterSparkId)
    val code = "println(\"hello world\")"
    var waitResult = true
    var result = "none"
    val handler = new ExecutionHandler {
      override def onError(exResult: ExecutionResults): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "fail"
          monitor.notifyAll()
        }
      }

      override def onOutput(result: OutputResponse, isAppend: Boolean): Unit = {
        assert(result.data.isEmpty || result.data == "hello world\n")
      }

      override def onSuccess(executionResults: ExecutionResults): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "success"
          monitor.notifyAll()
        }
      }

      override def onProgress(progress: Double): Unit = {}

      override def onUpdateExecutionStatus(status: String): Unit = {}
    }
    //    zeppelinService.runCode(code, handler, zeppelinService.getOrCreateNotebook(runCodeTestNotebook))
    //    monitor.synchronized {
    //      while (waitResult) {
    //        monitor.wait(20 * 1000)
    //      }
    //    }
    assert(result == "success")
  }
}