package cons

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import play.api.libs.json.{JsNull, JsValue, Json, JsObject}

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
          val byteArr = data.utf8String
          var firstWord = byteArr.split(" ").head
         println(s"server response - ${data.utf8String}")
//          println(s"first word - .${firstWord}.")
          if (firstWord == "{\n") {
//          val jsObject = Json.parse(byteArr).as[JsObject]
          val json: JsValue = Json.parse(byteArr)
          val key = (json \ "key").getOrElse(JsNull).toString()
            val message = (json \ "message").getOrElse(JsNull).toString()
            println(s"key sent from server: ${key}")
            println(s"server response - ${message}")
            sender ! Write(ByteString(key))
          }
//          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
  }
}