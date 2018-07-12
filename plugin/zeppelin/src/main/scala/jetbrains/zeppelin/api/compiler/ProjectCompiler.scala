package jetbrains.zeppelin.api.compiler

/**
  * Interface of the project compiler, which is implemented by different plugins
  */
trait ProjectCompiler {
  /**
    * Compile the current project and package it to a jar
    *
    * @param path - root project, which should be packed
    * @return path to the jar file
    */
  def compileAndPackage(path: String): String = {
    ""
  }
}