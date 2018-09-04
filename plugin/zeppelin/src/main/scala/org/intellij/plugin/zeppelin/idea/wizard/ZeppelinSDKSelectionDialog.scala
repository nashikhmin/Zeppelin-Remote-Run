package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import javax.swing.JComponent
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.dependency.{LibraryDescriptor, ZeppelinDependenciesManager}
import org.intellij.plugin.zeppelin.utils.ThreadRun

import scala.util.{Failure, Success, Try}

class ZeppelinSDKSelectionDialog(parent: JComponent, project: Project) extends ZeppelinSDKSelectionDialogBase(parent) {
  override protected def onOK(): Unit = {
    val selectedZeppelinVersion: String = versionList.getSelectedItem.toString
    val sparkVersion = ZeppelinComponent.connectionFor(project).sparkVersion

    val result: Try[LibraryDescriptor] = ThreadRun
      .withProgressSynchronouslyTry(s"Downloading Zeppelin Dependencies...") { it => {
        val indicator: ProgressIndicator = it.getProgressIndicator
        ZeppelinDependenciesManager.getZeppelinSdkDescriptor(selectedZeppelinVersion, sparkVersion,indicator)
      }
      }
    result match {
      case Failure(exception) => {
        showErrorDialog(contentPane, exception.getMessage, "Error Downloading Zeppelin Dependencies")
      }
      case Success(library) => {
        selectedSdk = library
      }
    }
    dispose()
  }
}

object ZeppelinSDKSelectionDialog {
  def apply(parent: JComponent, project: Project): ZeppelinSDKSelectionDialog = {
    new ZeppelinSDKSelectionDialog(parent,
      project)
  }
}