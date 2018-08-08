package jetbrains.zeppelin.dependency

import com.intellij.openapi.progress._
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.api.idea.IdeaCommonApi
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.utils.ThreadRun

/**
  * Class which import user dependencies for a Zeppelin interpreter
  *
  * @param project - a current project
  */
class ImportZeppelinInterpreterDependencies(project: Project) {
  def invoke(): Unit = {
    val manager = ProgressManager.getInstance
    val module = IdeaCommonApi.getCurrentModule(project)
    if (module == null) return
    val task = new Task.Backgroundable(project, "Adding interpreter dependencies", false) {
      override def run(indicator: ProgressIndicator): Unit = {
        indicator.setText("Zeppelin: loading list of user dependencies...")
        val jars: List[String] = getInterpreterUserDependenciesList

        indicator.setText("Zeppelin: downloading the dependencies from remote repo...")
        ThreadRun.invokeLater {
          ThreadRun.inWriteAction {
            ZeppelinDependenciesManager.importUserInterpreterLibrary(module, jars)
          }
        }
      }
    }
    manager.runProcessWithProgressAsynchronously(
      task, ImportZeppelinInterpreterDependencies.createBackgroundIndicator(project, "Zeppelin")
    )
  }

  private def getInterpreterUserDependenciesList = {
    val connection = ZeppelinComponent.connectionFor(project)
    val service = connection.service
    val interpreter = service.getDefaultInterpreter.get
    val jars = interpreter.dependencies.map(_.groupArtifactVersion)
    jars
  }
}

object ImportZeppelinInterpreterDependencies {
  def apply(project: Project): ImportZeppelinInterpreterDependencies = new ImportZeppelinInterpreterDependencies(project)


  private def createBackgroundIndicator(project: Project, name: String): ProgressIndicator = {
    Option(ProgressIndicatorProvider.getGlobalProgressIndicator).getOrElse(
      new BackgroundableProcessIndicator(
        project, name, PerformInBackgroundOption.ALWAYS_BACKGROUND, null, null, false
      )
    )
  }
}