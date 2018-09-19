package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import javax.swing.Icon

open class ZeppelinModuleType : JavaModuleType(ID) {
    override fun getDescription(): String = ZeppelinConstants.MODULE_DESCRIPTION
    override fun getName(): String = ZeppelinConstants.MODULE_NAME
    override fun getNodeIcon(isOpened: Boolean): Icon = ZeppelinConstants.MODULE_ICON

    companion object {
        val ID: String = ZeppelinConstants.MODULE_ID
        fun getModuleType(): ModuleType<*> = ModuleTypeManager.getInstance().findByID(ID)
        val instance: ZeppelinModuleType = ZeppelinModuleType()
    }
}