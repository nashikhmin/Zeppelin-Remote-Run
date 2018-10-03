package org.intellij.plugin.zeppelin.idea.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.ZeppelinException
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

/**
 * Console that handle all zeppelin messages
 */
class InterpretersView(val project: Project) : JBScrollPane(), Disposable {
    val innerList = JBList<String>()

    enum class PopupItem(val value: String) {
        RESTART_INTERPRETER("Restart"),
        SETTINGS("Settings"),
        SET_DEFAULT("Set default"),
        SYNCHRONIZE("Synchronize")
    }

    override fun dispose() {}

    init {
        this.setViewportView(innerList)
        innerList.emptyText.text = "Please, update the list of the interpreter"
        initPopupItemMenu()
    }

    private fun getSelectedValue(): String = innerList.selectedValue

    fun updateInterpretersList(interpreters: List<String>) {
        val interpretersArray = if (interpreters.isNotEmpty()) {
            val defaultElement = "${interpreters.first()} (default)"
            listOf(defaultElement) + interpreters.drop(1)
        } else {
            interpreters
        }.toTypedArray()
        val model = JBList.createDefaultListModel(*interpretersArray)
        innerList.model = model
    }

    private fun initPopupItemMenu() {
        val popupMenu = JPopupMenu()
        PopupItem.values().forEach {
            val item = JMenuItem(it.value)
            item.addActionListener { action -> popupElementAction(action) }
            popupMenu.add(item)
        }

        innerList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(me: MouseEvent) {
                val index = innerList.locationToIndex(me.point)
                innerList.selectedIndex = index
                if (index == -1) return
                if (SwingUtilities.isRightMouseButton(me)) popupMenu.show(innerList, me.x, me.y)
                if (me.clickCount == 2) setDefaultInterpreter()
            }
        })
    }

    private fun openSettingsForm() {
        ZeppelinComponent.connectionFor(project).service.openSettingsForm(getSelectedValue())
    }

    private fun popupElementAction(e: ActionEvent) {
        val item = PopupItem.values().find { it.value == e.actionCommand } ?: throw ZeppelinException(
                "Cannot parse an interpreter view menu element")

        when (item) {
            PopupItem.RESTART_INTERPRETER -> restartInterpreter()
            PopupItem.SETTINGS -> openSettingsForm()
            PopupItem.SET_DEFAULT -> setDefaultInterpreter()
            PopupItem.SYNCHRONIZE -> ZeppelinComponent.connectionFor(project).service.synchronizeInterpreter()
        }
    }

    private fun restartInterpreter() {
        ZeppelinComponent.connectionFor(project).service.restartInterpreter(getSelectedValue())
    }

    private fun setDefaultInterpreter() {
        val interpreterName = getSelectedValue()
        val connection = ZeppelinComponent.connectionFor(project)
        connection.service.setDefaultInterpreter(interpreterName)
        connection.updateInterpreterList(true)
    }
}