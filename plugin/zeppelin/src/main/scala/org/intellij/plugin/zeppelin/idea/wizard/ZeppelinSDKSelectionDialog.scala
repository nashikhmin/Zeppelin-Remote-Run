package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.Messages.showErrorDialog
import javax.swing.JComponent
import org.intellij.plugin.zeppelin.dependency.{LibraryDescriptor, ZeppelinDependenciesManager}
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.utils.ThreadRun

import scala.util.{Failure, Success, Try}

class ZeppelinSDKSelectionDialog(parent: JComponent) extends ZeppelinSDKSelectionDialogBase(parent) {
  override def open(): LibraryDescriptor = {
    super.open()
    val selectedZeppelinVersion: String = versionList.getSelectedItem.toString
    val sparkVersion = SparkVersion.ZEPPELIN_DEFAULT_VERSION

    val result: Try[LibraryDescriptor] = ThreadRun
      .withProgressSynchronouslyTry(s"Downloading Zeppelin Dependencies...") { it => {
        val indicator: ProgressIndicator = it.getProgressIndicator
        ZeppelinDependenciesManager.getZeppelinSdkDescriptor(selectedZeppelinVersion, sparkVersion, indicator)
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
    selectedSdk
  }

  override protected def onOK(): Unit = {    dispose()}
}

object ZeppelinSDKSelectionDialog {
  def apply(parent: JComponent): ZeppelinSDKSelectionDialog = {
    new ZeppelinSDKSelectionDialog(parent)
  }
}