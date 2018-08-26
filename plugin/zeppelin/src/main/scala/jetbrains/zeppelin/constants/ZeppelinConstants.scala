package jetbrains.zeppelin.constants

import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
  * Common constants for Zeppelin plugin
  */
object ZeppelinConstants {
  val MODULE_NAME = "Zeppelin module"
  val MODULE_DESCRIPTION = "A module, which allows to run code on Zeppelin"
  val MODULE_ID = "ZEPPELIN_MODULE"
  val MODULE_ICON: Icon = AllIcons.Nodes.Module

  val ADD_DEPENDENCY_TITLE = "Add an interpreter dependency"
  val EDIT_DEPENDENCY_TITLE = "Edit an interpreter dependency"
  val DEPENDENCY_NAME_LABEL = "group:id:version/file path"

  val ADD_NOTEBOOK_TITLE = "Add a notebook"
  val NOTEBOOK_NAME_LABEL = "Notebook name"


  val DEFAULT_NOTEBOOK_NAME = "IDEA_Plugin/default"
}