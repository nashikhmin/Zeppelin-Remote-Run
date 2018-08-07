package jetbrains.zeppelin.wizard

import com.intellij.openapi.ui.Messages.showErrorDialog
import javax.swing.JComponent
import jetbrains.zeppelin.utils.ThreadRun
import jetbrains.zeppelin.utils.dependency.{LibraryDescriptor, ZeppelinDependenciesManager}

import scala.util.{Failure, Success, Try}

class ZeppelinSDKSelectionDialog(parent: JComponent) extends ZeppelinSDKSelectionDialogBase(parent) {
  override protected def onDownload(): Unit = {
    val result: Try[LibraryDescriptor] = ThreadRun
      .withProgressSynchronouslyTry(s"Downloading Zeppelin Dependencies...") { _ => {
        ZeppelinDependenciesManager.getZeppelinSdkDescriptor("0.8.0")
      }
      }
    result match {
      case Failure(exception) => {
        showErrorDialog(contentPane, exception.getMessage, "Error Downloading Zeppelin Dependencies")
      }
      case Success(library) => {
        sdks.add(library)
        updateTable()
      }
    }
  }
}

object ZeppelinSDKSelectionDialog {
  def apply(parent: JComponent): ZeppelinSDKSelectionDialog = new ZeppelinSDKSelectionDialog(parent)
}