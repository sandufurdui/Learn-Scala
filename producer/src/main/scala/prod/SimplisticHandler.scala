package prod

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString

class SimplisticHandler extends Actor {
  import Tcp._
  def receive: Receive = {
    case Received(data) =>
      println(s"Data received - ${data.utf8String}")  //if received smth, prints decoded received data
      sender() ! Write(ByteString("SERVER_RES: ").concat(data)) //sends back
    case PeerClosed     => context stop self
  }
}
