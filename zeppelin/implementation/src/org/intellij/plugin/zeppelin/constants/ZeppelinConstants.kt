package org.intellij.plugin.zeppelin.constants

import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
  * Common constants for Zeppelin plugin
  */
object ZeppelinConstants {
    @JvmField
    val MODULE_NAME: String = "Zeppelin module"
    @JvmField
    val MODULE_DESCRIPTION: String = "A module, which allows to run code on Zeppelin"
    @JvmField
    val MODULE_ID: String = "ZEPPELIN_MODULE"
    @JvmField
    val MODULE_ICON: Icon = AllIcons.Nodes.Module

    @JvmField
    val ADD_DEPENDENCY_TITLE: String = "Add an interpreter dependency"
    @JvmField
    val EDIT_DEPENDENCY_TITLE: String = "Edit an interpreter dependency"
    @JvmField
    val DEPENDENCY_NAME_LABEL: String = "group:id:version/file path"
    @JvmField
    val ADD_NOTEBOOK_TITLE: String = "Add a notebook"
    @JvmField
    val NOTEBOOK_NAME_LABEL: String = "Notebook name"
    @JvmField
    val DEFAULT_NOTEBOOK_NAME: String = "IDEA_Plugin/default"
    @JvmField
    val PLUGIN_ID: String = "org.intellij.plugin.zeppelin"
    @JvmField
    val NOTEBOOK_BROWSER_CHANGE_CONFIRMATION_TITLE: String = "Change notebooks confirmations"
    @JvmField
    val PARAGRAPH_RUNNED: String = "--------RUN PARAGRAPH--------\n" + "Code:%s\nOutput:\n"
    @JvmField
    val PARAGRAPH_COMPLETED: String = "-----PARAGRAPH COMPLETED-----\n"
    @JvmField
    val PARAGRAPH_ERROR: String = "-------PARAGRAPH ERROR-------\n"
    @JvmField
    val RESTART_CONNECTION: String = "-----------------------------\nRestart connection to Zeppelin..."

    val ERROR_PROJECT_IS_NOT_SPECIFIED = "The project is not specified"
}