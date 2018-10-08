package org.intellij.plugin.zeppelin.constants

import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
  * Common constants for Zeppelin plugin
  */
object ZeppelinConstants {
    const val MODULE_NAME: String = "Zeppelin module"
    const val MODULE_DESCRIPTION: String = "A module, which allows to run code on Zeppelin"
    const val MODULE_ID: String = "ZEPPELIN_MODULE"
    @JvmField
    val MODULE_ICON: Icon = AllIcons.Nodes.Module

    const val ADD_DEPENDENCY_TITLE: String = "Add an interpreter dependency"
    const val EDIT_DEPENDENCY_TITLE: String = "Edit an interpreter dependency"
    const val DEPENDENCY_NAME_LABEL: String = "group:id:version/file path"
    const val ADD_NOTEBOOK_TITLE: String = "Add a notebook"
    const val NOTEBOOK_NAME_LABEL: String = "Notebook name"
    const val DEFAULT_NOTEBOOK_NAME: String = "IDEA_Plugin/default"
    const val PLUGIN_ID: String = "org.intellij.plugin.zeppelin"
    const val NOTEBOOK_BROWSER_CHANGE_CONFIRMATION_TITLE: String = "Change notebooks confirmations"
    const val PARAGRAPH_IS_RUN: String = "--------RUN PARAGRAPH--------\n" + "Code:\n%s\nOutput:\n"
    const val PARAGRAPH_COMPLETED: String = "-----PARAGRAPH COMPLETED-----\n"
    const val PARAGRAPH_ERROR: String = "-------PARAGRAPH ERROR-------\n"
    const val RESTART_CONNECTION: String = "-----------------------------\nRestart connection to Zeppelin..."

    const val ERROR_PROJECT_IS_NOT_SPECIFIED = "The project is not specified"
}