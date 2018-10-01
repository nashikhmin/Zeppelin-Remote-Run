package org.intellij.plugin.zeppelin.dependency

data class Dependency(val group: String, val id: String, val version: String,
                      val excludes: List<Exclusion> = emptyList())

data class Exclusion(val group: String, val id: String)
