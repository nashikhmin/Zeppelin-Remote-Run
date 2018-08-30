package org.intellij.plugin.zeppelin.service

import java.util.concurrent.Executors

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{DialogWrapper, Messages}
import com.intellij.psi.PsiManager
import jetbrains.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.api.websocket.{OutputHandler, OutputResult}
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.idea.settings.interpreter.InterpreterSettingsDialog
import org.intellij.plugin.zeppelin.models.{Interpreter, _}
import org.intellij.plugin.zeppelin.utils.{ThreadRun, ZeppelinLogger}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/**
  * Main class that implement logic of the actions for the communication with Zeppelin
  */
class ZeppelinActionService(project: Project, zeppelinSettings: ZeppelinSettings) {
  var zeppelinService: ZeppelinAPIService = ZeppelinAPIService(zeppelinSettings.address,
    zeppelinSettings.port,
    zeppelinSettings.user)

  /**
    * Method that close all connections and free resources
    */
  def destroy(): Unit = {
    zeppelinService.close()
  }

  /**
    * Get a default interpreter for the current notebook
    *
    * @return an interpreter
    */
  def getDefaultInterpreter: Option[Interpreter] = {
    if (!checkPreconditions()) return None

    val notebook = linkedNotebook
    zeppelinService.defaultInterpreter(notebook.id)
  }

  /**
    * Get interpreter settings by interpreter id
    *
    * @param interpreterId          - an id of an interpreter
    * @param ignoreInterpreterError - ignore interpreters errors
    * @return an interpreter model
    */
  def getInterpreterById(interpreterId: String, ignoreInterpreterError: Boolean = false): Interpreter = {
    zeppelinService.interpreterById(interpreterId, ignoreInterpreterError)
  }

  /**
    * Get interpreter settings for a notebook by name
    *
    * @param interpreterName - a name of an interpreter
    * @return an interpreter model
    */
  def getInterpreterByName(interpreterName: String): Interpreter = {
    val interpreter = interpreterList().find(_.name == interpreterName.split(" ").head).get
    interpreter
  }

  /**
    * Get a model of a notebook by id
    *
    * @param notebookId - an id of a notebook
    * @return an option with notebook
    */
  def getNotebookById(notebookId: String): Option[Notebook] = {
    if (!checkPreconditions()) return None
    zeppelinService.getNotebookById(notebookId)
  }

  /**
    * Get list of notebooks
    *
    * @return a list of notebooks
    */
  def getNotebooksList: List[Notebook] = {
    if (!checkPreconditions()) return List()
    zeppelinService.allNotebooks
  }

  /**
    * Get a list of available interpreters for the notebook
    *
    * @return the list with interpreters
    */
  def interpreterList(): List[Interpreter] = {
    if (!checkPreconditions()) return List()

    val allInterpreters = zeppelinService.allInterpreters
    val defaultInterpreter = getDefaultInterpreter

    if (defaultInterpreter.isEmpty) throw new ZeppelinException()
    defaultInterpreter.get +: allInterpreters.filter(_.id != defaultInterpreter.get.id)
  }

  /**
    * Open interpreter settings
    *
    * @param interpreterName - a name of an interpreter
    */
  def openSettingsForm(interpreterName: String): Unit = {
    val interpreter = getInterpreterByName(interpreterName)
    new InterpreterSettingsDialog(project, interpreter).show()
  }

  /**
    * Restart an interpreter
    *
    * @param interpreterName - a name of an interpreter
    */
  def restartInterpreter(interpreterName: String): Unit = {
    if (!checkPreconditions()) return

    ThreadRun.withProgressSynchronouslyTry(s"Restart an $interpreterName interpreter")(_ => {
      val interpreter: Interpreter = getInterpreterByName(interpreterName)
      val notebook = linkedNotebook
      zeppelinService.restartInterpreter(interpreter.id, notebook.id)
    })
  }

  /**
    * Run code on the Zeppelin server
    *
    * @param code - code, that be executed
    */
  def runCode(code: String): Unit = {
    if (!checkPreconditions()) return

    if (code.isEmpty) {
      ZeppelinLogger.printMessage("The selected text is empty, please select a piece of code")
      return
    }

    ZeppelinLogger.printMessage(s"Run paragraph with text: $code")
    val handler = new OutputHandler {
      override def onError(result: ExecutionResults = ExecutionResults()): Unit = {
        ZeppelinLogger.printError("Error during execution of a paragraph")
        result.msg.foreach(it => ZeppelinLogger.printError(it.data))

      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        if (result.data.isEmpty) return
        ZeppelinLogger.printMessage(result.data)
      }

      override def onSuccess(executionResults: ExecutionResults = ExecutionResults()): Unit = {
        executionResults.msg.foreach(it => {
          it.resultType match {
            case "TABLE" => {
              val handler = TableOutputHandler.getAll.headOption.getOrElse(DefaultTableOutputHandler)
              handler.invoke(project, it.data)
            }
            case _ => Unit
          }
        })
        ZeppelinLogger.printMessage("Paragraph is completed")
      }
    }
    zeppelinService.runCode(code, handler, linkedNotebook)
  }

  /**
    * Set the selected interpreter as a default interpreter for the notebook
    *
    * @param interpreterName - an interpreter name
    */
  def setDefaultInterpreter(interpreterName: String): Unit = {
    val interpreter: Interpreter = getInterpreterByName(interpreterName)
    val notebook = linkedNotebook
    zeppelinService.setDefaultInterpreter(notebook.id, interpreter.id)
  }

  /**
    * Upload new interpreter settings
    *
    * @param interpreter - a model of an interpreter
    */
  def updateInterpreterSettings(interpreter: Interpreter): Unit = {
    if (!checkPreconditions()) return

    val oldInterpreterSettings = getInterpreterById(interpreter.id, ignoreInterpreterError = true)
    val oldDependencies = oldInterpreterSettings.dependencies
    val newDependencies = interpreter.dependencies

    val removedDependencies = oldDependencies.filter(it => !newDependencies.contains(it))
    val addedDependencies = newDependencies.filter(it => !oldDependencies.contains(it))

    if (removedDependencies.nonEmpty) {
      ZeppelinLogger.printMessage("The next dependencies will be removed:")
      removedDependencies.foreach(it => ZeppelinLogger.printMessage(it.groupArtifactVersion))
    }

    if (addedDependencies.nonEmpty) {
      ZeppelinLogger.printMessage("The next dependencies will be added:")
      addedDependencies.foreach(it => ZeppelinLogger.printMessage(it.groupArtifactVersion))
    }


    // single threaded execution context
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    val f = Future {
      zeppelinService.updateInterpreterSetting(interpreter)
    }

    f.onComplete {
      result => {
        if (result.isSuccess) {
          ZeppelinLogger
            .printSuccess("Interpreter settings were updated." +
              s" Removed: ${removedDependencies.size}." +
              s" Added: ${addedDependencies.size}.")
        }
        if (result.isFailure) {
          ZeppelinLogger.printError("The error during the updating")
        }
      }
    }
  }

  /**
    * Update a list of notebooks on Zeppelin
    *
    * @param newNotebookList - a list of notebooks which must be on Zeppelin.
    *                        Some notebooks can be without id, they will be created.
    * @return added notebooks in Zeppelin
    */
  def updateNotebooksTo(newNotebookList: List[Notebook]): List[Notebook] = {
    if (!checkPreconditions()) throw ZeppelinConnectionException(zeppelinSettings.fullUrl)

    val originalNotebooks = zeppelinService.allNotebooks.toSet

    val notebooksForRemove = originalNotebooks.diff(newNotebookList.toSet)
    val notebooksForAdd = newNotebookList.filter(_.id.isEmpty)
    if (notebooksForAdd.isEmpty && notebooksForRemove.isEmpty) return originalNotebooks.toList

    val confirmationMsg = s"Do you really want to add ${notebooksForAdd.size} " +
      s"and remove ${notebooksForRemove.size} notebooks?\n"
    val deletedMsg = "\nThe next notebooks will be deleted:\n" +
      notebooksForRemove.map(it => it.name).mkString("\n") + "\n"
    val addedMsg = "\nThe next notebooks will be added:\n" + notebooksForAdd.map(it => it.name).mkString("\n")
    val msg = confirmationMsg +
      (if (notebooksForRemove.nonEmpty) deletedMsg else "") +
      (if (notebooksForAdd.nonEmpty) addedMsg else "")

    val exitCode = Messages.showYesNoDialog(
      project,
      msg,
      ZeppelinConstants.NOTEBOOK_BROWSER_CHANGE_CONFIRMATION_TITLE,
      Messages.getQuestionIcon)
    if (exitCode != DialogWrapper.OK_EXIT_CODE) return originalNotebooks.toList

    notebooksForRemove.foreach(it => {
      zeppelinService.deleteNotebook(it)
    })
    newNotebookList.map(note => {
      if (note.id.nonEmpty) {
        note
      }
      else {
        zeppelinService.createNotebook(note.name)
      }
    })
  }

  /**
    * Test the connection and try to connect if the server is disconnected
    *
    * @return the server is connected
    */
  private def checkConnection: Boolean = {
    if (zeppelinService.isConnected) return true
    try {
      zeppelinService.close()
      zeppelinService = ZeppelinAPIService(zeppelinSettings.address, zeppelinSettings.port, zeppelinSettings.user)
      zeppelinService.connect(false)
    }
    catch {
      case _: ZeppelinConnectionException => {
        ZeppelinLogger
          .printError(s"Connection error. Check that ${zeppelinSettings.fullUrl} is available")
      }
      case _: ZeppelinLoginException => ZeppelinLogger.printError(s"Authentication error. Check login and password")
    }
    zeppelinService.isConnected
  }

  /**
    * Check that an user open any file for the notebook
    *
    * @return a file is open or not
    */
  private def checkOpenFile: Boolean = {
    try {
      linkedNotebook
      true
    }
    catch {
      case _: NoSelectedFilesException => {
        ZeppelinLogger.printError("Please, open any file, a notebook will be created for a specific file")
        false
      }
    }
  }

  /**
    * Check preconditions before the performing a request
    *
    * @return a request can be performed or not
    */
  private def checkPreconditions(): Boolean = {
    checkConnection && checkOpenFile
  }

  /**
    * Get a notebook for the current opened file
    *
    * @return a id of a file
    */
  private def linkedNotebook: Notebook = {
    val file = FileEditorManager.getInstance(project).getSelectedEditor.getFile
    val psiFile = PsiManager.getInstance(project).findFile(file)
    val maybeHolder = FileNotebookHolder.getAll.find(_.contains(psiFile))
    if (maybeHolder.isDefined) {
      val noteId = maybeHolder.get.notebookId(psiFile)
      zeppelinService.getNotebookById(noteId).getOrElse(throw NotebookNotFoundException(noteId))
    } else {
      zeppelinService.getOrCreateNotebook(zeppelinSettings.defaultNotebookName)
    }
  }
}

object ZeppelinActionService {
  def apply(project: Project, zeppelinSettings: ZeppelinSettings): ZeppelinActionService = {
    new ZeppelinActionService(project, zeppelinSettings)
  }
}