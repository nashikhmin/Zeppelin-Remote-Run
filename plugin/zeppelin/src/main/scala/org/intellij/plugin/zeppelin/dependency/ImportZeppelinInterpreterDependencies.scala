package org.intellij.plugin.zeppelin.dependency

import com.intellij.openapi.progress._
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.api.idea.IdeaCommonApi
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.utils.ThreadRun

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
        indicator.setText("Loading list of user dependencies...")
        val jars: List[String] = getInterpreterUserDependenciesList

        indicator.setText("Downloading the dependencies from remote repo...")
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

  private def getInterpreterUserDependenciesList: List[String] = {
    val connection = ZeppelinComponent.connectionFor(project)
    val service = connection.service
    val interpreter = service.getDefaultInterpreter
    if (interpreter.isEmpty) return List()
    val jars = interpreter.get.dependencies.map(_.groupArtifactVersion)
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