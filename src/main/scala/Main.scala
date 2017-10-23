import java.util.concurrent.TimeUnit

import MapActor.MovingTagRequest
import Models.{Anchor, Coordinate, MapObject}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, JsObject, RootJsonFormat}

import scala.concurrent.Future


object Main extends App with JsonSupport{
  import akka.http.scaladsl.server.Directives._
  import akka.pattern._
  import spray.json._

  implicit val actorSystem = ActorSystem("server")
  implicit val mat = ActorMaterializer()
  implicit val ex = actorSystem.dispatcher
  implicit val askTimeout = Timeout(5, TimeUnit.SECONDS)

  val anchors = Config.ANCHORS.map(kv => Anchor(kv._1, kv._2)).toList
  val mapper = actorSystem.actorOf(Props(classOf[MapActor], anchors))
  val udpServer = actorSystem.actorOf(Props(classOf[UdpServer], Config.UDPSERVER.url, Config.UDPSERVER.port, mapper))

  val routes: Route = pathPrefix("api") {
    pathPrefix("data") {
      val movingObjects = (mapper ? MovingTagRequest).asInstanceOf[Future[Seq[Models.MovingObject]]]
      onSuccess(movingObjects){
        d =>
          val l: Seq[MapObject] = d ++ anchors
          complete(l.toJson)
      }
    }
  } ~ pathSingleSlash {
    getFromResource("static/index.html")
  } ~ {
    encodeResponse {
      getFromResourceDirectory("static")
    }
  }

  val adminApiBindingFuture: Future[ServerBinding] = Http()
    .bindAndHandle(routes, Config.WEBSERVER.url, Config.WEBSERVER.port)
    .map(binding => {
      println("Server started")
      binding
    })
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import spray.json._
  implicit val coordinateJsonFormat: RootJsonFormat[Coordinate] = jsonFormat2(Coordinate.apply)
  implicit val mapObjectJsonFormat: RootJsonFormat[MapObject] = MapObjectJsonFormat

  implicit object MapObjectJsonFormat extends RootJsonFormat[MapObject] {
    def write(c: MapObject) =
      JsObject(
       "name" -> c.name.toJson,
       "coordinate" -> c.coordinate.toJson,
       "tagType" -> c.tagType.toJson,
       "age"-> c.age.toJson,
       "stability"-> c.stability.toJson
      )

    def read(value: JsValue): MapObject = ???
    /*
      value.asJsObject.getFields("name", "coordinate", "tagType", "age", "stability") match {
      case Seq(JsString(name), ) =>
        new Color(name, red.toInt, green.toInt, blue.toInt)
      case _ => deserializationError("")
    }
    */
  }
}
