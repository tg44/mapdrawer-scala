import Models.AnchorData
import org.scalatest.{Matchers, WordSpecLike}

class MapActorSpec extends WordSpecLike with Matchers{

  val testData1 = AnchorData("tag", "a1", 1, 1, 100)
  val testData2 = AnchorData("tag", "a2", 1, 1, 200)
  val testData3 = AnchorData("tag", "a3", 1, 1, 300)
  val testData4 = AnchorData("tag", "a4", 1, 1, 400)


  "MapActor" when {

    "#addNewData" should {

      "add element to a list if the list is empty" in {
        MapActor.addNewData(testData1, Seq()) shouldBe Seq(testData1)
      }

      "rewrite element if the anchor is present" in {
        MapActor.addNewData(testData1, Seq(testData1.copy(time = 50))) shouldBe Seq(testData1)
      }

      "drop last element if we have more than 3 (check sorted too)" in {
        MapActor.addNewData(testData4, Seq(testData1, testData2, testData3)) shouldBe Seq(testData2, testData3, testData4)
      }

    }

  }
}
