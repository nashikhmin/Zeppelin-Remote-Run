package org.intellij.plugin.zeppelin.dependency

import java.util

import coursier.{Dependency, Module}

import scala.collection.JavaConverters._

/**
  * A class which resolve default Zeppelin dependencies
  */
object DefaultZeppelinDependencies {
  val ZEPPELIN_SUPPORTED_VERSIONS = List("0.8.0")

  private val avroExclusions = Set(
    ("io.netty", "netty"),
    ("org.mortbay.jetty", "jetty"),
    ("org.mortbay.jetty", "jetty-util"),
    ("org.mortbay.jetty", "servlet-api"),
    ("org.apache.velocity", "velocity"),
  )
  private val hadoopExclusions = Set(
    ("asm", "asm"),
    ("org.ow2.asm", "asm"),
    ("org.jboss.netty", "netty"),
    ("commons-logging", "commons-logging"),
    ("javax.servlet", "servlet-api"),
  )

  /**
    * Get Zeppelin dependencies as Java list
    *
    * @param version - get required versions of the libraries
    * @return a set with dependencies
    */
  def getDefaultZeppelinDependencies(version: ZeppelinDependenciesVersions): Set[Dependency] = {
    hadoopDependencies(version) ++
      sparkDependencies(version) ++
      avroDependencies(version) ++
      otherDependencies(version)
  }

  /**
    * Get a list of supported Zeppelin versions
    *
    * @return a Java list with versions
    */
  def getSupportedZeppelinVersionsAsJava: util.List[String] = {
    ZEPPELIN_SUPPORTED_VERSIONS.asJava
  }

  private def avroDependencies(version: ZeppelinDependenciesVersions): Set[Dependency] = {
    Set(
      Dependency(
        Module("org.apache.avro", "avro"), version.avro
      ),
      Dependency(
        Module("org.apache.avro", "avro-ipc"), version.avro,
        exclusions = avroExclusions
      ),
      Dependency(
        Module("org.apache.avro", "avro-mapred"), version.avro,
        exclusions = avroExclusions
      ),
    )
  }

  private def hadoopDependencies(version: ZeppelinDependenciesVersions): Set[Dependency] = {
    Set(
      Dependency(
        Module("org.apache.hadoop", "hadoop-yarn-api"), version.yarn,
        exclusions = hadoopExclusions
      ),
      Dependency(
        Module("org.apache.hadoop", "hadoop-yarn-client"), version.yarn,
        exclusions = hadoopExclusions
      ),
      Dependency(
        Module("org.apache.hadoop", "hadoop-yarn-common"), version.yarn,
        exclusions = hadoopExclusions
      ),
      Dependency(
        Module("org.apache.hadoop", "hadoop-yarn-server-web-proxy"), version.yarn,
        exclusions = hadoopExclusions
      ),
    )
  }

  private def otherDependencies(version: ZeppelinDependenciesVersions) = {
    Set(
      Dependency(
        Module("net.java.dev.jets3t", "jets3t"), version.jets3,
        exclusions = Set(
          ("commons-logging", "commons-logging"),
        )
      ),
    )
  }

  private def sparkDependencies(version: ZeppelinDependenciesVersions): Set[Dependency] = {
    Set(
      Dependency(
        Module("org.apache.spark", s"spark-yarn_${version.scala}"), version.yarn
      ),
      Dependency(
        Module("org.apache.spark", s"spark-catalyst_${version.scala}"), version.spark
      ),
      Dependency(
        Module("org.apache.spark", s"spark-streaming_${version.scala}"), version.spark
      ),
      Dependency(
        Module("org.apache.spark", s"spark-hive_${version.scala}"), version.spark
      ),
      Dependency(
        Module("org.apache.spark", s"spark-sql_${version.scala}"), version.spark
      ),
      Dependency(
        Module("org.apache.spark", s"spark-repl_${version.scala}"), version.spark
      ),
      Dependency(
        Module("org.apache.spark", s"spark-core_${version.scala}"), version.spark,
        exclusions = Set(
          ("org.apache.hadoop", "hadoop-client"),
        )
      ),
    )
  }
}


private case class ZeppelinDependenciesVersions(scala: String,
                                                spark: String,
                                                avro: String = "1.7.7",
                                                jets3: String = "0.7.1",
                                                hadoop: String = "2.3.0",
                                                yarn: String = "2.3.0",
                                                akka: String = "2.3.4-spark")