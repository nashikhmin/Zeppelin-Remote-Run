package jetbrains.zeppelin.models

/**
  * Factory, which create Zeppelin models
  */
object ZeppelinModelFactory {
  def createNotebook(name: String) = Notebook("", name)
}