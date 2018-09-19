package org.intellij.plugin.zeppelin.models

import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

class SparkVersion(public val versionString: String) {
    private val version: Int = parseVersion()

    fun getProgress1_0(): Boolean = this.olderThan(SparkVersion.SPARK_1_1_0)

    fun hasDataFrame(): Boolean = this.newerThanEquals(SparkVersion.SPARK_1_4_0)

    fun isPysparkSupported(): Boolean = this.newerThanEquals(SparkVersion.SPARK_1_2_0)

    fun isSecretSocketSupported(): Boolean = this.newerThanEquals(SparkVersion.SPARK_2_3_1)

    fun isSpark2(): Boolean = this.newerThanEquals(SparkVersion.SPARK_2_0_0)

    fun isSparkRSupported(): Boolean = this.newerThanEquals(SparkVersion.SPARK_1_4_0)

    fun isUnsupportedVersion(): Boolean {
        return olderThan(SparkVersion.MIN_SUPPORTED_VERSION) || newerThanEquals(SparkVersion.UNSUPPORTED_FUTURE_VERSION)
    }

    fun newerThan(versionToCompare: SparkVersion): Boolean = version > versionToCompare.version

    fun newerThanEquals(versionToCompare: SparkVersion): Boolean = version >= versionToCompare.version

    fun oldLoadFilesMethodName(): Boolean = this.olderThan(SparkVersion.SPARK_1_3_0)

    fun oldSqlContextImplicits(): Boolean = this.olderThan(SparkVersion.SPARK_1_3_0)

    fun olderThan(versionToCompare: SparkVersion): Boolean = version < versionToCompare.version

    fun olderThanEquals(versionToCompare: SparkVersion): Boolean = version <= versionToCompare.version

    fun toNumber(): Int = version

    override fun toString(): String = versionString

    private fun parseVersion(): Int {
        return try {
            val numberParts = versionString.split('.')

            val major: Int = numberParts[0].toInt()
            val minor: Int = numberParts[1].toInt()
            val patch: Int = numberParts[2].toInt()

            "%d%02d%02d".format(major, minor, patch).toInt()
        } catch (e: Exception) {
            ZeppelinLogger.printError("Can not recognize Spark version $versionString. Assume it's a future release")
            99999
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SparkVersion) return false

        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        return version
    }

    /**
    * Provide reading comparing capability of spark version returned from SparkContext.version()
    */
    companion object {
        val SPARK_1_0_0: SparkVersion = SparkVersion("1.0.0")
        val SPARK_1_1_0: SparkVersion = SparkVersion("1.1.0")
        val SPARK_1_2_0: SparkVersion = SparkVersion("1.2.0")
        val SPARK_1_3_0: SparkVersion = SparkVersion("1.3.0")
        val SPARK_1_4_0: SparkVersion = SparkVersion("1.4.0")
        val SPARK_1_5_0: SparkVersion = SparkVersion("1.5.0")
        val SPARK_1_6_0: SparkVersion = SparkVersion("1.6.0")
        val SPARK_2_0_0: SparkVersion = SparkVersion("2.0.0")
        val SPARK_2_2_0: SparkVersion = SparkVersion("2.2.0")
        val SPARK_2_3_1: SparkVersion = SparkVersion("2.3.1")
        val SPARK_2_4_0: SparkVersion = SparkVersion("2.4.0")
        val MIN_SUPPORTED_VERSION: SparkVersion = SPARK_1_0_0
        val UNSUPPORTED_FUTURE_VERSION: SparkVersion = SPARK_2_4_0
        val ZEPPELIN_DEFAULT_VERSION: SparkVersion = SPARK_2_2_0
    }
}