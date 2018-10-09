package org.intellij.plugin.zeppelin.idea.settings.interpreter

import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import org.intellij.plugin.zeppelin.models.InterpreterProperty
import org.intellij.plugin.zeppelin.models.InterpreterPropertyType

class InterpreterPropertiesTableModel : ListTableModel<InterpreterProperty>(
        object : ColumnInfo<InterpreterProperty, String>("Name") {
            override fun valueOf(item: InterpreterProperty): String? = item.name
        },
        object : ColumnInfo<InterpreterProperty, String>("Value") {
            override fun valueOf(item: InterpreterProperty): String? {
                return item.value.toString()
            }

            override fun isCellEditable(item: InterpreterProperty?): Boolean = true

            override fun setValue(item: InterpreterProperty, value: String) {
                item.value  = when (item.type) {
                    InterpreterPropertyType.TEXTAREA, InterpreterPropertyType.STRING,
                    InterpreterPropertyType.URL, InterpreterPropertyType.PASSWORD ->  value
                    InterpreterPropertyType.NUMBER -> value.toIntOrNull()?:item.value
                    InterpreterPropertyType.CHECKBOX -> value.toBoolean()
                }
            }
        })