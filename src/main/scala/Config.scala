
import Models.Coordinate
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

object Config {

  case class ServerConfig(url: String, port: Int)


  private[this] implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  import pureconfig.loadConfigOrThrow

  lazy val WEBSERVER: ServerConfig = loadConfigOrThrow[ServerConfig]("webserver")
  lazy val UDPSERVER: ServerConfig = loadConfigOrThrow[ServerConfig]("udpserver")

  lazy val ANCHORS: Map[String, Coordinate] = loadConfigOrThrow[Map[String, Coordinate]]("anchors")
}
