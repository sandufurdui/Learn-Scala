package broker

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer

class SimplisticHandler extends Actor {
  import Tcp._
  val b = new ArrayBuffer[String]()
  def receive: Receive = {
    case Received(data) =>
      var response = data.utf8String
//      println(response)

//      b = b + response
//      b.insert(0, "ffff ")
      b.prepend(response)
      println(b.size)
    //if (response == "subscribe") {
//        var subscribeText = "user subscribed confirmation"
//        sender() ! Write(ByteString(subscribeText))
//        sender() ! Write(ByteString(response))
//      }
//      if (response == "unsubscribe") {
//        var subscribeText = "user unsubscribed confirmation"
//        sender() ! Write(ByteString(subscribeText))
//      }
//      if (response == "stop") {
//        var subscribeText = "closing connectioon"
//        sender() ! Write(ByteString(subscribeText))
//        context stop self
//      }
    //      println(s"Data received - ${data.utf8String}")  //if received smth, prints decoded received data
    //      sender() ! Write(ByteString("SERVER_RES: ").concat(data)) //sends back
    case PeerClosed     => context stop self
  }
}
