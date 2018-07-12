package jetbrains.zeppelin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.components.{JBList, JBScrollPane}
import javax.swing.JComponent

/**
  * Console that handle all zeppelin messages
  */
class InterpretersView extends JBScrollPane with Disposable {
  override def dispose(): Unit = {}

  val innerList = new JBList[String]()
  this.setViewportView(innerList)
  innerList.getEmptyText.setText("Please, update the list of the interpreter")

  def updateInterpretersList(interpreters: Iterable[String]): Unit = {
    val defaultElement = s"${interpreters.head} (default)"
    val interpretersWithDefault: Array[String] = defaultElement +: interpreters.drop(1).toList.toArray
    val model = JBList.createDefaultListModel(interpretersWithDefault: _*)
    innerList.setModel(model)
  }

  def getSelectedValue: String = innerList.getSelectedValue

  def getComponent: JComponent = {
    this
  }
}
