package undertaker

import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val httpConfig = config.getConfig("http")
    val httpHost = httpConfig.getString("host")
    val httpPort = httpConfig.getInt("port")

    println(s"Config: $httpHost, $httpPort")
  }

}
