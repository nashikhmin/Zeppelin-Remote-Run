package org.intellij.plugin.zeppelin.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.intellij.plugin.zeppelin.models.ConvertException
import org.intellij.plugin.zeppelin.models.ParseException

object JsonParser {
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    fun toJson(value: Any): String = moshi.adapter(Any::class.java).toJson(value) ?: throw  ConvertException(
            value.toString())

    fun <T> fromValueObject(value: Any, clazz: Class<T>): T {
        return moshi.adapter(clazz).fromJsonValue(value) ?: throw ParseException(value.toString(),
                clazz::class.toString())
    }

    fun <T> fromStringObject(json: String, clazz: Class<T>): T {
        return moshi.adapter(clazz).fromJson(json) ?: throw ParseException(json, clazz::class.toString())
    }

    fun <T> fromValueList(value: Any, clazz: Class<T>): List<T> {
        val fromJsonValue = moshi.adapter(List::class.java).fromJsonValue(value)
        val jsonList = fromJsonValue?.map { it as Any } ?: listOf()
        return jsonList.map { fromValueObject(it, clazz) }
    }

    fun <T> fromValueMap(value: Any, clazz: Class<T>): Map<String,T?> {
        val fromJsonValue = moshi.adapter(Map::class.java).fromJsonValue(value)
        val jsonMap = fromJsonValue?.map { it -> it.key as String to it.value as Any }?.toMap()?: mapOf()
        return jsonMap.map { it.key to fromValueObject(it.value, clazz) }.toMap()
    }
}
