package jetbrains.zeppelin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.components.JBList
import javax.swing.JComponent

/**
  * Console that handle all zeppelin messages
  */
class InterpretersView extends JBList[String] with Disposable {
  override def dispose(): Unit = {}

  def updateInterpretersList(interpreters: Iterable[String]): Unit = {
    val model = JBList.createDefaultListModel(interpreters.toList.toArray: _*)
    this.setModel(model)
  }

  def getComponent: JComponent = {
    this
  }
}
