package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.icons.AllIcons.Icons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory
import javax.swing.Icon

class ZeppelinProjectTemplatesFactory : ProjectTemplatesFactory() {
    override fun createTemplates(group: String?, context: WizardContext?): Array<ProjectTemplate> {
        return arrayOf(ZeppelinProjectTemplate())
    }

    override fun getGroupIcon(group: String): Icon = Icons.Ide.NextStep

    override fun getGroups(): Array<String> = arrayOf(ZeppelinProjectTemplatesFactory.GROUP)

    companion object {
        const val GROUP: String = "Remote Execute"
    }
}