package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages.showErrorDialog
import org.intellij.plugin.zeppelin.dependency.dependency.ZeppelinInterpreterDependencies
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.utils.withProgressSynchronously
import javax.swing.JComponent

class ZeppelinSDKSelectionDialog(parent: JComponent) : ZeppelinSDKSelectionDialogBase(parent) {
    override fun open(): org.intellij.plugin.zeppelin.dependency.dependency.LibraryDescriptor? {
        super.open()
        val selectedZeppelinVersion: String = versionList.selectedItem.toString()
        val sparkVersion = SparkVersion.ZEPPELIN_DEFAULT_VERSION

        val result = withProgressSynchronously("Downloading Zeppelin Dependencies...") {
            val indicator: ProgressIndicator = ProgressManager.getInstance().progressIndicator
            ZeppelinInterpreterDependencies.getZeppelinSdkDescriptor(selectedZeppelinVersion, sparkVersion, indicator)
        }

        if (result == null) showErrorDialog(contentPane, "TODO://Message!", "Error Downloading Zeppelin Dependencies")
        selectedSdk = result
        return selectedSdk
    }

    override fun onOK(): Unit {
        dispose()
    }
}

