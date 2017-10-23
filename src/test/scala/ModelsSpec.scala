import Models.{Circle, Coordinate}
import org.scalatest.{Matchers, WordSpecLike}

class ModelsSpec extends WordSpecLike with Matchers{

  "Circle" must {

    "calc intersect point if we only have one" in {
      val c1 = Circle(Coordinate(0,20),10)
      val c2 = Circle(Coordinate(20,20),10)
      val (p1,p2) = c1 intersects c2
      Seq(p1, p2) should contain( Coordinate(10,20) )
    }

    "calc intersect points if we have two" in {
      val c1 = Circle(Coordinate(10,10),5)
      val c2 = Circle(Coordinate(16,10),5)
      val (p1,p2) = c1 intersects c2
      Seq(p1, p2) should contain( Coordinate(13,14) )
      Seq(p1, p2) should contain( Coordinate(13,6) )
    }

  }
}
