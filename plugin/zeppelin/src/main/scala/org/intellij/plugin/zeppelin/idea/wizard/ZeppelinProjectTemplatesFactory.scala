package org.intellij.plugin.zeppelin.idea.wizard


import com.intellij.icons.AllIcons.Icons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.platform.{ProjectTemplate, ProjectTemplatesFactory}
import javax.swing.Icon

class ZeppelinProjectTemplatesFactory extends ProjectTemplatesFactory {
  def createTemplates(group: String,
                      context: WizardContext): Array[ProjectTemplate] = {
    Array(new ZeppelinProjectTemplate())
  }

  override def getGroupIcon(group: String): Icon = Icons.Ide.NextStep

  def getGroups: Array[String] = Array(ZeppelinProjectTemplatesFactory.Group)
}

object ZeppelinProjectTemplatesFactory {
  val Group = "Remote Execute"
}