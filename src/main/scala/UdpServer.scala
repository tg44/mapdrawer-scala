import java.net.InetSocketAddress

import Models.AnchorData
import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import scodec.{Attempt, Codec, DecodeResult, codecs}


//reference implementation from https://doc.akka.io/docs/akka/current/scala/io-udp.html#bind-and-send-
class UdpServer(ip: String, port: Int, nextActor: ActorRef) extends Actor {
  import context.system
  import scodec.interop.akka._

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(ip, port))

  override def receive: Receive = {
    case Udp.Bound(local) =>
      println("udp listening started")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val processed: Attempt[DecodeResult[UdpServer.RawUdpData]] = UdpServer.codec.decode(data.toByteVector.toBitVector)
      //println(processed)
      //println(data)
      processed.fold(
        _ => () ,
        _.value.toAnchorData(System.currentTimeMillis()).foreach(nextActor ! _)
      )
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}

object UdpServer {
  import scodec._
  import codecs._

  // itIsADistance, movingTagName1, movingTagName2, anchorName1, anchorTagName2,  distance, a, b, c, d, e, f= unpack('BssssHBBBBBB', data)
  case class RawUdpData(
                         itIsADistance: Short,
                         movingTagName1: String,
                         movingTagName2: String,
                         anchorName1: String,
                         anchorName2: String,
                         distance: Int,
                         a: Short,
                         b: Short,
                         c: Short,
                         d: Short,
                         e: Short,
                         f: Short) {
    def toAnchorData(time: Long): Option[AnchorData] = {
      if(itIsADistance != 2) {
        None
      } else {
        Option(
          //todo quality is probably calculated wrong
          AnchorData(movingTagName1 + movingTagName2, anchorName1 + anchorName2, distance, a+b+c+d+e,time)
        )
      }
    }
  }

  //https://stackoverflow.com/questions/2667714/parsing-of-binary-data-with-scala
  val tupleCodec: Codec[Short ~ String ~ String ~ String ~ String ~ Int ~ Short ~ Short ~ Short ~ Short ~ Short ~ Short] =
    ushort8 ~ fixedSizeBits(8L, utf8) ~ fixedSizeBits(8L, utf8) ~ fixedSizeBits(8L, utf8) ~ fixedSizeBits(8L, utf8) ~ uint16 ~ ushort8 ~ ushort8 ~ ushort8 ~ ushort8 ~ ushort8 ~ ushort8

  val codec: Codec[RawUdpData] = tupleCodec.xmap[RawUdpData]({
    case itIsADistance ~ movingTagName1 ~ movingTagName2 ~ anchorName1 ~ anchorName2 ~ distance ~ a ~ b ~ c ~ d ~ e ~ f =>
      RawUdpData(itIsADistance, movingTagName1, movingTagName2, anchorName1, anchorName2, distance, a, b, c, d, e, f)
  }, st => st.itIsADistance ~ st.movingTagName1 ~ st.movingTagName2 ~ st.anchorName1 ~ st.anchorName2 ~ st.distance ~ st.a ~ st.b ~ st.c ~ st.d ~ st.e ~ st.f)
}
