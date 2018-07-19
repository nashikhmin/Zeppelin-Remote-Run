package jetbrains.zeppelin.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.components.{JBList, JBScrollPane}
import javax.swing.JComponent

/**
  * Console that handle all zeppelin messages
  */
class InterpretersView extends JBScrollPane with Disposable {
  val innerList = new JBList[String]()

  override def dispose(): Unit = {}
  this.setViewportView(innerList)
  innerList.getEmptyText.setText("Please, update the list of the interpreter")

  def getComponent: JComponent = {
    this
  }

  def getSelectedValue: String = innerList.getSelectedValue

  def updateInterpretersList(interpreters: Iterable[String]): Unit = {
    val defaultElement = s"${interpreters.head} (default)"
    val interpretersWithDefault: Array[String] = defaultElement +: interpreters.drop(1).toList.toArray
    val model = JBList.createDefaultListModel(interpretersWithDefault: _*)
    innerList.setModel(model)
  }
}