package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants

/**
  * An extension point for creating templates
  */
trait TemplateFileCreator {
  /**
    * Create a template
    * @param sourceFolder - a full path to source folder
    * @param project - an IDEA project
    */
  def create(sourceFolder: String, project: Project)
}

object TemplateFileCreator {
  val Id: String = ZeppelinConstants.PLUGIN_ID + ".templateFileCreator"
  val EP_NAME: ExtensionPointName[TemplateFileCreator] =
    ExtensionPointName.create[TemplateFileCreator](Id)

  def getAll: Array[TemplateFileCreator] = EP_NAME.getExtensions
}