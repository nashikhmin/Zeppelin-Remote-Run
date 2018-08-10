package jetbrains.zeppelin.idea.toolwindow

import java.awt.event.{ActionEvent, MouseAdapter, MouseEvent}

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.{JBList, JBScrollPane}
import javax.swing.{JComponent, JMenuItem, JPopupMenu, SwingUtilities}
import jetbrains.zeppelin.components.ZeppelinComponent

/**
  * Console that handle all zeppelin messages
  */
class InterpretersView(project: Project) extends JBScrollPane with Disposable {
  val innerList = new JBList[String]()

  object PopupItem extends Enumeration {
    type PopupItem = Value
    val RESTART_INTERPRETER: PopupItem.Value = Value("Restart")
    val SETTINGS: PopupItem.Value = Value("Settings")
    val SET_DEFAULT: PopupItem.Value = Value("Set default")
  }

  override def dispose(): Unit = {}

  this.setViewportView(innerList)
  innerList.getEmptyText.setText("Please, update the list of the interpreter")
  initPopupItemMenu()

  def getComponent: JComponent = {
    this
  }

  def getSelectedValue: String = innerList.getSelectedValue

  def updateInterpretersList(interpreters: Iterable[String]): Unit = {
    val interpretersArray = if (interpreters.nonEmpty) {
      val defaultElement = s"${interpreters.head} (default)"
      defaultElement +: interpreters.drop(1).toList.toArray
    } else {
      interpreters.toArray
    }
    val model = JBList.createDefaultListModel(interpretersArray: _*)
    innerList.setModel(model)
  }

  private def initPopupItemMenu(): Unit = {
    val refreshItem = new JMenuItem(PopupItem.RESTART_INTERPRETER.toString)
    refreshItem.addActionListener((e: ActionEvent) => popupElementAction(e))

    val settingsItem = new JMenuItem(PopupItem.SETTINGS.toString)
    settingsItem.addActionListener((e: ActionEvent) => popupElementAction(e))

    val makeDefaultItem = new JMenuItem(PopupItem.SET_DEFAULT.toString)
    makeDefaultItem.addActionListener((e: ActionEvent) => popupElementAction(e))

    val popupMenu = new JPopupMenu
    popupMenu.add(refreshItem)
    popupMenu.add(makeDefaultItem)
    popupMenu.add(new JPopupMenu.Separator())
    popupMenu.add(settingsItem)

    innerList.addMouseListener(new MouseAdapter() {
      override def mouseClicked(me: MouseEvent): Unit = {
        val index = innerList.locationToIndex(me.getPoint)
        innerList.setSelectedIndex(index)
        if (index == -1) return
        if (SwingUtilities.isRightMouseButton(me)) popupMenu.show(innerList, me.getX, me.getY)
        if (me.getClickCount == 2) setDefaultInterpreter()
      }
    })
  }

  private def openSettingsForm(): Unit = {
    ZeppelinComponent.connectionFor(project).service.openSettingsForm(getSelectedValue)
  }

  private def popupElementAction(e: ActionEvent): Unit = {
    val item: PopupItem.Value = PopupItem.withName(e.getActionCommand)
    item match {
      case PopupItem.RESTART_INTERPRETER => restartInterpreter()
      case PopupItem.SETTINGS => openSettingsForm()
      case PopupItem.SET_DEFAULT => setDefaultInterpreter()
      case _ => throw new Exception("Not implemented popup element")
    }
  }

  private def restartInterpreter(): Unit = {
    ZeppelinComponent.connectionFor(project).service.restartInterpreter(getSelectedValue)
  }

  private def setDefaultInterpreter(): Unit = {
    val interpreterName = getSelectedValue
    val connection = ZeppelinComponent.connectionFor(project)
    connection.service.setDefaultInterpreter(interpreterName)
    connection.updateInterpreterList()
  }
}