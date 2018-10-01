package org.intellij.plugin.zeppelin.dependency

/**
 * A class which resolve default Zeppelin dependencies
 */
object DefaultZeppelinDependencies {
    @JvmStatic
    val ZEPPELIN_SUPPORTED_VERSIONS = listOf("0.8.0")

    private val avroExclusions = listOf(
            Exclusion("io.netty", "netty"),
            Exclusion("org.mortbay.jetty", "jetty"),
            Exclusion("org.mortbay.jetty", "jetty-util"),
            Exclusion("org.mortbay.jetty", "servlet-api"),
            Exclusion("org.apache.velocity", "velocity")
            )
    private val hadoopExclusions = listOf(
            Exclusion("asm", "asm"),
            Exclusion("org.ow2.asm", "asm"),
            Exclusion("org.jboss.netty", "netty"),
            Exclusion("commons-logging", "commons-logging"),
            Exclusion("javax.servlet", "servlet-api")
    )

    /**
     * Get Zeppelin dependencies as Java list
     *
     * @param version - get required versions of the libraries
     * @return a set with dependencies
     */
    fun getDefaultZeppelinDependencies(version: ZeppelinDependenciesVersions): List<Dependency> {
        return zeppelinDependencies(version) +
                hadoopDependencies(version) +
                sparkDependencies(version) +
                avroDependencies(version) +
                otherDependencies(version)
    }

    private fun avroDependencies(version: ZeppelinDependenciesVersions): List<Dependency> = listOf(
            Dependency("org.apache.avro", "avro", version.avro),
            Dependency("org.apache.avro", "avro-ipc", version.avro, avroExclusions),
            Dependency("org.apache.avro", "avro-mapred", version.avro, avroExclusions)
    )

    private fun hadoopDependencies(version: ZeppelinDependenciesVersions): List<Dependency> = listOf(
            Dependency("org.apache.hadoop", "hadoop-yarn-api", version.yarn, hadoopExclusions),
            Dependency("org.apache.hadoop", "hadoop-yarn-client", version.yarn, hadoopExclusions),
            Dependency("org.apache.hadoop", "hadoop-yarn-common", version.yarn, hadoopExclusions),
            Dependency("org.apache.hadoop", "hadoop-yarn-server-web-proxy", version.yarn, hadoopExclusions)
    )

    private fun otherDependencies(version: ZeppelinDependenciesVersions) = listOf(
            Dependency("net.java.dev.jets3t", "jets3t", version.jets3, listOf(
                    Exclusion("commons-logging", "commons-logging")))
    )

    private fun sparkDependencies(version: ZeppelinDependenciesVersions): List<Dependency> = listOf(
            Dependency("org.apache.spark", "spark-yarn_${version.scala}", version.yarn),
            Dependency("org.apache.spark", "spark-catalyst_${version.scala}", version.spark),
            Dependency("org.apache.spark", "spark-streaming_${version.scala}", version.spark),
            Dependency("org.apache.spark", "spark-hive_${version.scala}", version.spark),
            Dependency("org.apache.spark", "spark-sql_${version.scala}", version.spark),
            Dependency("org.apache.spark", "spark-repl_${version.scala}", version.spark),
            Dependency("org.apache.spark", "spark-core_${version.scala}", version.spark, listOf(
                    Exclusion("org.apache.hadoop", "hadoop-client")))
    )

    private fun zeppelinDependencies(version: ZeppelinDependenciesVersions): List<Dependency> = listOf(
            Dependency(
                    "org.apache.zeppelin", "spark-interpreter", version.zeppelin, listOf(
                    Exclusion("org.apache.hadoop", "hadoop-client")))
    )
}

data class ZeppelinDependenciesVersions(val scala: String,
                                        val spark: String,
                                        val zeppelin: String,
                                        val avro: String = "1.7.7",
                                        val jets3: String = "0.7.1",
                                        val hadoop: String = "2.3.0",
                                        val yarn: String = "2.3.0",
                                        val akka: String = "2.3.4-spark")