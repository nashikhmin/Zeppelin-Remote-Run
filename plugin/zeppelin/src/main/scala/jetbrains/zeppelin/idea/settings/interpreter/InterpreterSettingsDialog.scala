package jetbrains.zeppelin.idea.settings.interpreter

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.{JComponent, SwingConstants}
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.models.{Dependency, InstantiationType, Interpreter, InterpreterOption}

import scala.collection.JavaConverters._

class InterpreterSettingsDialog(project: Project, var interpreter: Interpreter) extends DialogWrapper(project) {
  val title = s"${interpreter.name} interpreter settings"
  private val myPanel = new InterpreterSettingsForm()

  setTitle(title)
  setButtonsAlignment(SwingConstants.CENTER)
  init()

  override def createCenterPanel(): JComponent = myPanel.getContentPane

  override def doOKAction(): Unit = {
    val newDependencies: List[Dependency] = getNewDependencies
    val newOptions = getNewOptions

    interpreter = interpreter.copy(dependencies = newDependencies, option = newOptions)

    val connection = ZeppelinComponent.connectionFor(project)
    val actionService = connection.service
    actionService.updateInterpreterSettings(interpreter)

    UpdateInterpreterHandler.getAll.foreach(_.updateInterpreter(project))
    super.doOKAction()
  }

  override def init(): Unit = {
    super.init()
    updateDependencyList()
    updateInstantiationType()
  }

  def updateDependencyList(): Unit = {
    val dependencies = interpreter.dependencies.map(_.groupArtifactVersion)
    myPanel.initDataModel(dependencies.asJava)
  }

  def updateInstantiationType(): Unit = {
    val options = interpreter.option
    val values: List[String] = InstantiationType.values.toList.map(_.toString)
    myPanel.initInstantiationTypes(values.asJava, options)
  }

  private def getNewDependencies = {
    val dependenciesNames = myPanel.getModelList.asScala.toList
    val originalDependencies = interpreter.dependencies
    val newDependencies = dependenciesNames
      .filter(_ != null)
      .map(it => {
        originalDependencies
          .find(_.groupArtifactVersion == it)
          .getOrElse(Dependency(it))
      })
    newDependencies
  }

  private def getNewOptions = {
    if (myPanel.isGlobally) {
      InterpreterOption()
    } else {
      InterpreterOption(Some(myPanel.getPerNoteValue), Some(myPanel.getPerUserValue))
    }
  }
}