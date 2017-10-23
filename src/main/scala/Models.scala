object Models {

  case class Coordinate(x: Double, y: Double) {
    def +(other: Coordinate) = {
      Coordinate(x + other.x, y + other.y)
    }

    def -(other: Coordinate) = {
      this + (other * -1)
    }

    def *(scalar: Double) = {
      Coordinate(x * scalar, y * scalar)
    }

    def /(scalar: Double) = {
      this * (1.0 / scalar)
    }

    def distance(other: Coordinate) = {
      Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2))
    }
  }

  case class Circle(coordinate: Coordinate, r: Double) {
    def x: Double = coordinate.x
    def y: Double = coordinate.y

    //https://gamedev.stackexchange.com/questions/7172/how-do-i-find-the-intersections-between-colliding-circles
    def intersects(other: Circle) = {
      val d = coordinate distance other.coordinate
      //check if an intersect is possible (and rise error if not)
      require(! (d > r + other.r || d == 0 || d < Math.abs(r - other.r)) )

      val a = (Math.pow(r, 2) - Math.pow(other.r, 2) + Math.pow(d, 2) ) / (2 * d)
      val b = d - a
      val h = Math.sqrt(Math.pow(r, 2) - Math.pow(a, 2))

      val helperPoint1 = coordinate + (other.coordinate - coordinate) * a / d
      val helperPoint2 = (other.coordinate - coordinate) * h / d

      val point1 = Coordinate(helperPoint1.x + helperPoint2.y, helperPoint1.y - helperPoint2.x)
      val point2 = Coordinate(helperPoint1.x - helperPoint2.y, helperPoint1.y + helperPoint2.x)

      (point1, point2)
    }
  }

  trait MapObject{
    val name: String
    val coordinate: Coordinate
    val tagType: String
    val age: Long
    val stability: Long
  }
  object MapObject {
    def apply(name: String, coordinate: Coordinate, tagType: String, age: Long, stability: Long): MapObject = {
      if(tagType == ANCHOR) {
        Anchor(name, coordinate)
      } else {
        MovingObject(name, coordinate, tagType, age, stability)
      }
    }
  }
  case class MovingObject(name: String, coordinate: Coordinate, tagType: String, age: Long, stability: Long) extends MapObject
  case class Anchor(name: String, coordinate: Coordinate, tagType: String = ANCHOR, age: Long=0, stability: Long = 0) extends MapObject

  case class AnchorData(movingTagName: String, anchorName: String, distnace: Int, quality: Integer, time: Long)

  val ANCHOR = "anchor"
  val MOVING = "moving"
}
