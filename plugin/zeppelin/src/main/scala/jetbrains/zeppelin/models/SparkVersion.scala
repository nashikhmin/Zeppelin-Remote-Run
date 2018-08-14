package jetbrains.zeppelin.models

import jetbrains.zeppelin.utils.ZeppelinLogger

class SparkVersion(var versionString: String) {
  private val version: Int = parseVersion

  override def equals(versionToCompare: Any): Boolean = version == versionToCompare.asInstanceOf[SparkVersion].version

  def getProgress1_0: Boolean = this.olderThan(SparkVersion.SPARK_1_1_0)

  def hasDataFrame: Boolean = this.newerThanEquals(SparkVersion.SPARK_1_4_0)

  def isPysparkSupported: Boolean = this.newerThanEquals(SparkVersion.SPARK_1_2_0)

  def isSecretSocketSupported: Boolean = this.newerThanEquals(SparkVersion.SPARK_2_3_1)

  def isSpark2: Boolean = this.newerThanEquals(SparkVersion.SPARK_2_0_0)

  def isSparkRSupported: Boolean = this.newerThanEquals(SparkVersion.SPARK_1_4_0)

  def isUnsupportedVersion: Boolean = {
    olderThan(SparkVersion.MIN_SUPPORTED_VERSION) ||
      newerThanEquals(SparkVersion.UNSUPPORTED_FUTURE_VERSION)
  }

  def newerThan(versionToCompare: SparkVersion): Boolean = version > versionToCompare.version

  def newerThanEquals(versionToCompare: SparkVersion): Boolean = version >= versionToCompare.version

  def oldLoadFilesMethodName: Boolean = this.olderThan(SparkVersion.SPARK_1_3_0)

  def oldSqlContextImplicits: Boolean = this.olderThan(SparkVersion.SPARK_1_3_0)

  def olderThan(versionToCompare: SparkVersion): Boolean = version < versionToCompare.version

  def olderThanEquals(versionToCompare: SparkVersion): Boolean = version <= versionToCompare.version

  def toNumber: Int = version

  override def toString: String = versionString

  private def parseVersion: Int = {
    try {
      val pos = versionString.indexOf('-')
      var numberPart = versionString
      if (pos > 0) numberPart = versionString.substring(0, pos)
      val versions = numberPart.split("\\.")
      val major = versions(0).toInt
      val minor = versions(1).toInt
      val patch = versions(2).toInt
      // version is always 5 digits. (e.g. 2.0.0 -> 20000, 1.6.2 -> 10602)
      "%d%02d%02d".format(major, minor, patch).toInt
      //    version = String.format("%d%02d%02d", major, minor, patch).toInt
    }
    catch {
      case e: Exception => {
        ZeppelinLogger.printError("Can not recognize Spark version " + versionString + ". Assume it's a future release")
        // assume it is future release
        99999
      }
    }
  }
}

/**
  * Provide reading comparing capability of spark version returned from SparkContext.version()
  */
object SparkVersion {
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

  def apply(versionString: String): SparkVersion = new SparkVersion(versionString)
}

