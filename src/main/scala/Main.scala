import MapActor.MovingTagRequest
import Models.{Anchor, Coordinate, MapObject}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future


object Main extends App with JsonSupport{
  import akka.http.scaladsl.server.Directives._
  import akka.pattern._
  import spray.json._

  implicit val actorSystem = ActorSystem("server")
  implicit val mat = ActorMaterializer()
  implicit val ex = actorSystem.dispatcher

  val anchors = Config.ANCHORS.map(kv => Anchor(kv._1, kv._2)).toList
  val mapper = actorSystem.actorOf(Props(classOf[MapActor], anchors))
  val udpServer = actorSystem.actorOf(Props(classOf[UdpServer], Config.UDPSERVER.url, Config.UDPSERVER.port, mapper))

  val routes: Route = pathPrefix("api") {
    pathPrefix("data") {
      val movingObjects = mapper ? MovingTagRequest
      complete(movingObjects.map(_ :: anchors).toJson)
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

  implicit val coordinateJsonFormat: RootJsonFormat[Coordinate] = jsonFormat2(Coordinate.apply)
  implicit val mapObjectJsonFormat: RootJsonFormat[MapObject] = jsonFormat5(MapObject.apply)
}
