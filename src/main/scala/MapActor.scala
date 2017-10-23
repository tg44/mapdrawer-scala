import MapActor.MovingTagRequest
import Models._
import akka.actor.Actor

import scala.collection.mutable
import scala.util.Try

class MapActor(anchors: Seq[Anchor]) extends Actor {

  val map = mutable.Map.empty[String, Seq[AnchorData]]

  override def receive: Receive = {
    case data: AnchorData =>
      map += data.movingTagName -> MapActor.addNewData(data, map.getOrElse(data.movingTagName, Seq()))
    case MovingTagRequest =>
      sender ! map.toList.flatMap(MapActor.createMapObject(_, anchors, System.currentTimeMillis()))
  }
}

object MapActor {

  case object MovingTagRequest

  def addNewData(data: AnchorData, list: Seq[AnchorData]) = {
    val newList = (list.filter(d => d.anchorName != data.anchorName) ++ Seq(data) ).sortBy(_.time)
    if(newList.size > 3) {
      newList.tail
    } else {
      newList
    }
  }

  def calcCoordinates(list: Seq[AnchorData], anchors: Seq[Anchor]) = {
    //check if we get enought informtion to calc the point
    require(list.size == 3)

    val circle1: Circle = Circle(anchors.find(_.name == list(0).anchorName).get.coordinate, list(0).distnace)
    val circle2: Circle = Circle(anchors.find(_.name == list(1).anchorName).get.coordinate, list(1).distnace)
    val circle3: Circle = Circle(anchors.find(_.name == list(2).anchorName).get.coordinate, list(2).distnace)

    //calc 2 intersection point between the first 2 anchor
    val (point1, point2) = circle1 intersects circle2

    //choose the better
    val acc1 = Math.abs((point1 distance circle3.coordinate) - circle3.r)
    val acc2 = Math.abs((point2 distance circle3.coordinate) - circle3.r)

    if(acc1 < acc2) {
      point1
    } else {
      point2
    }
  }

  def createMapObject(kv: (String, Seq[AnchorData]), anchors: Seq[Anchor], now: Long): Option[MovingObject] = {
    Try{
      MovingObject(kv._1, calcCoordinates(kv._2, anchors), Models.MOVING, now - kv._2.last.time, kv._2.last.time - kv._2.head.time)
    }.toOption
  }
}
