package jetbrains.zeppelin.wizard

import com.intellij.openapi.ui.Messages.showErrorDialog
import javax.swing.JComponent
import jetbrains.zeppelin.utils.ThreadRun
import jetbrains.zeppelin.utils.dependency.{LibraryDescriptor, ZeppelinDependenciesManager}

import scala.util.{Failure, Success, Try}

class ZeppelinSDKSelectionDialog(parent: JComponent) extends ZeppelinSDKSelectionDialogBase(parent) {
  override protected def onOK(): Unit = {
    val selectedVersion: String = versionList.getSelectedItem.toString
    val result: Try[LibraryDescriptor] = ThreadRun
      .withProgressSynchronouslyTry(s"Downloading Zeppelin Dependencies...") { _ => {
        ZeppelinDependenciesManager.getZeppelinSdkDescriptor(selectedVersion)
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
  def apply(parent: JComponent): ZeppelinSDKSelectionDialog = new ZeppelinSDKSelectionDialog(parent)
}