package org.intellij.plugin.zeppelin.models

case class ExecuteContext(text: String,
                          noteId: String,
                          paragraphId: String,
                          )

case class ExecutionOperation(id:String,
                              executeContext: ExecuteContext,
                              operations: List[ExecutionMessage] = List())

case class ExecutionMessage(code: String, data: String)