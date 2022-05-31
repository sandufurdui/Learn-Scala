package cons

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

case class receivedKey(y: String, key: Int)

object TcpClient {
  def props(remote: InetSocketAddress, listener: ActorRef): Props =
    Props(new TcpClient(remote, listener))
}

class TcpClient(remote: InetSocketAddress, var listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  if (listener == null) listener = Tcp.get(context.system).manager

  IO(Tcp) ! Connect(remote)

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      // listener ! c
      val connection = sender()
      connection ! Register(self)

      context become {
//        case receivedKey(str, ll) => print(s"idk message ${str} with key ${ll}")
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          println(s"server response - ${data.utf8String}")
//          var len = data.utf8String.toString.length
//          sender ! len
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}