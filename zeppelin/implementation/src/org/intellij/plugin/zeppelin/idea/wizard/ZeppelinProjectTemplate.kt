package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ProjectTemplate
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import javax.swing.Icon

open class ZeppelinProjectTemplate : ProjectTemplate {
    override fun validateSettings(): ValidationInfo? = null
    override fun createModuleBuilder(): ZeppelinModuleBuilder = ZeppelinModuleBuilder()
    override fun getDescription(): String = ZeppelinConstants.MODULE_DESCRIPTION
    override fun getIcon(): Icon = ZeppelinConstants.MODULE_ICON
    override fun getName(): String = ZeppelinConstants.MODULE_NAME
}