package org.intellij.plugin.zeppelin.idea.settings.interpreter

import com.intellij.ui.BooleanTableCellEditor
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import org.intellij.plugin.zeppelin.models.InterpreterProperty
import org.intellij.plugin.zeppelin.models.InterpreterPropertyType
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class InterpreterPropertiesTableModel : ListTableModel<InterpreterProperty>(
        object : ColumnInfo<InterpreterProperty, String>("Name") {
            override fun valueOf(item: InterpreterProperty): String? = item.name
        },
        object : ColumnInfo<InterpreterProperty, Any>("Value") {
            override fun valueOf(item: InterpreterProperty): Any {
                return item.value
            }

            override fun getRenderer(item: InterpreterProperty): TableCellRenderer? {
                if (item.type == InterpreterPropertyType.CHECKBOX)
                    return BooleanTableCellRenderer()
                return super.getRenderer(item)
            }

            override fun isCellEditable(item: InterpreterProperty?): Boolean = true

            override fun getEditor(item: InterpreterProperty): TableCellEditor? {
                if (item.type == InterpreterPropertyType.CHECKBOX)
                    return BooleanTableCellEditor()
                return super.getEditor(item)
            }

            override fun setValue(item: InterpreterProperty, value: Any) {
                val stringValue = value.toString()
                item.value = when (item.type) {
                    InterpreterPropertyType.TEXTAREA, InterpreterPropertyType.STRING,
                    InterpreterPropertyType.URL, InterpreterPropertyType.PASSWORD -> stringValue
                    InterpreterPropertyType.NUMBER -> (stringValue.toIntOrNull() ?: item.value)
                    InterpreterPropertyType.CHECKBOX -> stringValue.toBoolean()
                }
            }
        })