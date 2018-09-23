package org.intellij.plugin.zeppelin.service

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.experimental.launch
import org.intellij.plugin.zeppelin.api.remote.ZeppelinApi
import org.intellij.plugin.zeppelin.api.remote.ZeppelinIntegration
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.extensionpoints.FileNotebookHolder
import org.intellij.plugin.zeppelin.idea.settings.interpreter.InterpreterSettingsDialog
import org.intellij.plugin.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.models.*
import org.intellij.plugin.zeppelin.service.execution.GuiExecutionHandlerFactory
import org.intellij.plugin.zeppelin.service.execution.ZeppelinExecutionManager
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Main class that implement logic of the actions for the communication with Zeppelin
 */
class ZeppelinActionService(private val project: Project, private val zeppelinSettings: ZeppelinSettings) {
    private var integration = ZeppelinIntegration(zeppelinSettings,
            GuiExecutionHandlerFactory(project))
    val api: ZeppelinApi
        get() = integration.api
    private val executionManager: ZeppelinExecutionManager
        get() = integration.executionManager

    /**
     * Method that close all connections and free resources
     */
    fun destroy() {
        api.close()
    }

    /**
     * Get a default interpreter for the current notebook
     *
     * @return an interpreter
     */
    fun getDefaultInterpreter(): Interpreter? {
        if (!checkPreconditions()) return null
        val notebook: Notebook = linkedNotebook()
        return api.defaultInterpreter(notebook.id)
    }

    /**
     * Get interpreter settings by interpreter id
     *
     * @param interpreterId          - an id of an interpreter
     * @param ignoreInterpreterError - ignore interpreters errors
     * @return an interpreter model
     */
    fun getInterpreterById(interpreterId: String, ignoreInterpreterError: Boolean): Interpreter? {
        return api.interpreterById(interpreterId, ignoreInterpreterError)
    }

    /**
     * Get interpreter settings for a notebook by name
     *
     * @param interpreterName - a name of an interpreter
     * @return an interpreter model
     */
    fun getInterpreterByName(interpreterName: String): Interpreter? {
        if (!checkPreconditions()) return null
        return interpreterList().firstOrNull { it.name == interpreterName.split(" ")[0] }
    }

    /**
     * Get a model of a notebook by id
     *
     * @param notebookId - an id of a notebook
     * @return an option with notebook
     */
    fun getNotebookById(notebookId: String): Notebook? {
        if (!checkPreconditions()) return null
        return api.getNotebookById(notebookId)
    }

    /**
     * Get list of notebooks
     *
     * @return a list of notebooks
     */
    fun getNotebooksList(): List<Notebook> {
        if (!checkPreconditions()) return listOf()
        return api.allNotebooks()
    }

    /**
     * Get a list of available interpreters for the notebook
     *
     * @return the list with interpreters
     */
    fun interpreterList(): List<Interpreter> {
        if (!checkPreconditions()) return listOf()
        val allInterpreters: List<Interpreter> = api.allInterpreters()
        val defaultInterpreter: Interpreter? = getDefaultInterpreter() ?: return listOf()

        return allInterpreters + defaultInterpreter!!
    }

    /**
     * Open interpreter settings
     *
     * @param interpreterName - a name of an interpreter
     */
    fun openSettingsForm(interpreterName: String) {
        val interpreter: Interpreter = getInterpreterByName(interpreterName) ?: throw InterpreterNotFoundException(
                interpreterName)
        InterpreterSettingsDialog(project, interpreter).show()
    }

    /**
     * Restart an interpreter
     *
     * @param interpreterName - a name of an interpreter
     */
    fun restartInterpreter(interpreterName: String) {
        if (!checkPreconditions()) return

        val restartFunction = {
            val interpreter: Interpreter = getInterpreterByName(interpreterName) ?: throw InterpreterNotFoundException(
                    interpreterName)
            val notebook: Notebook = linkedNotebook()
            api.restartInterpreter(interpreter.id, notebook.id)
        }
        ProgressManager.getInstance().runProcessWithProgressSynchronously(restartFunction,
                "Restart an $interpreterName interpreter", false, project)
    }

    /**
     * Run code on the Zeppelin server
     *
     * @param text              - a text of an execution code
     * @param paragraphIdOption - an id of executing paragraph, if it is None, the paragraph will be created
     */
    fun runCode(text: String, paragraphIdOption: String?) {
        if (!checkPreconditions()) return
        if (text.isEmpty()) {
            ZeppelinLogger.printMessage("The selected text is empty, please select a piece of code")
            return
        }
        val paragraphId: String = paragraphIdOption ?: api.createParagraph(linkedNotebook().id, text).id
        val executeContext = ExecuteContext(text, linkedNotebook().id, paragraphId)
        executionManager.execute(executeContext)
    }

    /**
     * Set the selected interpreter as a default interpreter for the notebook
     *
     * @param interpreterName - an interpreter name
     */
    fun setDefaultInterpreter(interpreterName: String) {
        val interpreter: Interpreter? = getInterpreterByName(interpreterName)
        if (interpreter == null) {
            ZeppelinLogger.printError("Cannot get an interpreter with name $interpreterName")
            return
        }
        val notebook: Notebook = linkedNotebook()
        api.setDefaultInterpreter(notebook.id, interpreter.id)
    }

    /**
     * Upload new interpreter settings
     *
     * @param interpreter - a model of an interpreter
     */
    fun updateInterpreterSettings(interpreter: Interpreter) {
        if (!checkPreconditions()) return
        val oldInterpreterSettings = getInterpreterById(interpreter.id, true)

        if (oldInterpreterSettings == null) {
            ZeppelinLogger.printError("Cannot get interpreter settings")
            return
        }

        val oldDependencies: List<Dependency> = oldInterpreterSettings.dependencies
        val newDependencies: List<Dependency> = interpreter.dependencies
        val removedDependencies = oldDependencies.filter { it ->
            !newDependencies.contains(it)
        }
        val addedDependencies = newDependencies.filter { it ->
            !oldDependencies.contains(it)
        }
        if (removedDependencies.isNotEmpty()) {
            ZeppelinLogger.printMessage("The next dependencies will be removed:")
            removedDependencies.forEach { it -> ZeppelinLogger.printMessage(it.groupArtifactVersion) }
        }
        if (addedDependencies.isNotEmpty()) {
            ZeppelinLogger.printMessage("The next dependencies will be added:")
            addedDependencies.forEach { it -> ZeppelinLogger.printMessage(it.groupArtifactVersion) }
        }
        launch {
            api.updateInterpreterSetting(interpreter)
            ZeppelinLogger.printSuccess("Interpreter settings were updated." +
                    " Removed: ${removedDependencies.size}." +
                    " Added: ${addedDependencies.size}.")
        }
    }

    /**
     * Update a list of notebooks on Zeppelin
     *
     * @param newNotebookList - a list of notebooks which must be on Zeppelin.
     *                        Some notebooks can be without id, they will be created.
     * @return added notebooks in Zeppelin
     */
    fun updateNotebooksTo(newNotebookList: List<Notebook>): List<Notebook> {
        if (!checkPreconditions()) throw ZeppelinConnectionException(zeppelinSettings.fullUrl)

        val originalNotebooks = api.allNotebooks().toSet()
        val notebooksForRemove = originalNotebooks.minus(newNotebookList).toSet()
        val notebooksForAdd = newNotebookList.filter { it.id.isEmpty() }
        if (notebooksForAdd.isEmpty() && notebooksForRemove.isEmpty())
            return originalNotebooks.toList()

        val confirmationMsg: String = "Do you really want to add ${notebooksForAdd.size} " + "and remove ${notebooksForRemove.size} notebooks?\n"
        val deletedMsg: String = "\nThe next notebooks will be deleted:\n" + notebooksForRemove.joinToString(
                "\n") { it -> it.name } + "\n"
        val addedMsg: String = "\nThe next notebooks will be added:\n" + notebooksForAdd.joinToString(
                "\n") { it -> it.name }
        val msg: String = confirmationMsg +
                if (notebooksForRemove.isNotEmpty()) deletedMsg else "" +
                        if (notebooksForAdd.isNotEmpty()) addedMsg else ""

        val exitCode: Int = Messages.showYesNoDialog(project, msg,
                ZeppelinConstants.NOTEBOOK_BROWSER_CHANGE_CONFIRMATION_TITLE, Messages.getQuestionIcon())
        if (exitCode != DialogWrapper.OK_EXIT_CODE) return originalNotebooks.toList()

        notebooksForRemove.forEach { it -> api.deleteNotebook(it) }

        val notebooks: List<Notebook> = newNotebookList.map { note ->
            if (note.id.isNotBlank()) {
                note
            } else {
                api.createNotebook(note.name)
            }
        }
        return notebooks.sortedBy { it.name }
    }

    /**
     * Test the connection and try to connect if the server is disconnected
     *
     * @return the server is connected
     */
    private fun checkConnection(): Boolean {
        if (integration.isConnected()) return true
        try {
            integration.close()
            integration = ZeppelinIntegration(zeppelinSettings,
                    GuiExecutionHandlerFactory(project))
            integration.connect()
        } catch (_: ZeppelinConnectionException) {
            ZeppelinLogger.printError("Connection error. Check that ${zeppelinSettings.fullUrl} is available")
        } catch (_: ZeppelinLoginException) {
            ZeppelinLogger.printError("Authentication error. Check name and password")
        }
        return integration.isConnected()
    }

    /**
     * Check that an user open any file for the notebook
     *
     * @return a file is open or not
     */
    private fun checkOpenFile(): Boolean {
        return try {
            FileEditorManager.getInstance(project).selectedEditor != null
        } catch (_: NoSelectedFilesException) {
            ZeppelinLogger.printError("Please, open any file, a notebook will be created for a specific file")
            false
        }
    }

    /**
     * Check preconditions before the performing a request
     *
     * @return a request can be performed or not
     */
    private fun checkPreconditions(): Boolean {
        return checkConnection() && checkOpenFile()
    }

    /**
     * Get a notebook for the current opened file
     *
     * @return a id of a file
     */
    private fun linkedNotebook(): Notebook {
        val editor: FileEditor = FileEditorManager.getInstance(project).selectedEditor
                ?: return api.getOrCreateNotebook(zeppelinSettings.defaultNotebookName)

        val file: VirtualFile = editor.file ?: throw ZeppelinException("The editor does not contains a virtual file")
        val psiFile: PsiFile = runReadAction { PsiManager.getInstance(project).findFile(file)!! }
        val holder = FileNotebookHolder.getAll().find { it.contains(psiFile) }
        return if (holder != null) {
            val notebookId = holder.getNotebookId(psiFile)
            api.getNotebookById(notebookId) ?: throw NotebookNotFoundException(notebookId)
        } else {
            api.getOrCreateNotebook(zeppelinSettings.defaultNotebookName)
        }
    }

    companion object {
        fun create(project: Project, zeppelinSettings: ZeppelinSettings): ZeppelinActionService {
            val service = ZeppelinActionService(project, zeppelinSettings)
            service.checkConnection()
            return service
        }
    }
}