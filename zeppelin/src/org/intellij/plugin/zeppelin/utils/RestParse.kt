package org.intellij.plugin.zeppelin.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.intellij.plugin.zeppelin.models.ZeppelinException
import com.intellij.ide.PsiCopyPasteManager.MyData
import com.squareup.moshi.Types
import com.squareup.moshi.Types.newParameterizedType
import org.jetbrains.kotlin.utils.addToStdlib.cast

object RestParse {
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    fun <T> fromValueObject(value: Any, clazz: Class<T>): T {
        return moshi.adapter(clazz).fromJsonValue(value)?:throw ZeppelinException()
    }

    fun <T> fromValueList(value: Any, clazz: Class<T>): List<T> {
        val fromJsonValue = moshi.adapter(List::class.java).fromJsonValue(value)
        val jsonList = fromJsonValue?.map { it as Any}?: listOf()
        return jsonList.map { fromValueObject(it,clazz) }
    }
}
