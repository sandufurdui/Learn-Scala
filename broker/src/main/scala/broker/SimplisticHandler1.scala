package broker

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer

class SimplisticHandler1 extends Actor {
  import Tcp._
  val b = new ArrayBuffer[String]()
  def receive: Receive = {
    case Received(data) =>
      var response = data.utf8String
//           println(response)
      b.prepend(response)
      //      b = b + response
      //      b.insert(0, "ffff ")

      if (response == "subscribe") {
        sender() ! Write(ByteString("SERVER_RES: ").concat(data))
        var subscribeText = "user subscribed confirmation"
//        b.map(A)
//        b.toString()
                sender() ! Write(ByteString(b.toString()))
        //        sender() ! Write(ByteString(response))
      }
      else if (response == "unsubscribe") {
        var subscribeText = "user unsubscribed confirmation"
        sender() ! Write(ByteString(subscribeText))
      }
      //      if (response == "subscribe") {
      //        var subscribeText = "user subscribed confirmation"
      //        sender() ! Write(ByteString(subscribeText))
      //        sender() ! Write(ByteString(response))
      //      }
//      else {
//        //        var subscribeText = "closing connectioon"
//        //        sender() ! Write(ByteString(subscribeText))
//        //        context stop self

//        println(b.size)
//      }
              println(s"Data received - ${data.utf8String}")  //if received smth, prints decoded received data
    //sends back
    case PeerClosed     => context stop self
  }
}
