package broker

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

case class subscribeRequest(y: String)
case class updateRequest(y: String)

case class sendToClient(y: String, key: Int)

//case class Start(buf: Array[String]) {
//  override def toString = buf.mkString("->")
//}

class ClientWorker extends Actor {
  import Tcp._

  var TCPsender: ActorRef = sender()
  val b = new ArrayBuffer[String]()
  println(s"------------client worker started ${context.self}------------")
  def receive: Receive = {
    case qResponse(str) => {
      println(s"(client worker) queueManager response ${str}")
      TCPsender ! Write(ByteString(s"SERVER_RES: ${str}"))
    }
    case Received(data) => {
        var response = data.utf8String

      TCPsender = sender()
        b.prepend(response)
        if (response == "subscribe") {
          val response = data.utf8String
          print(response)
          context.actorSelection("akka://brokerSystem/user/queueManager").tell(subscribeRequest(response.toString), sender = context.self)
        }
        else if (response == "unsubscribe") {
          var unsubscribeText = "user unsubscribed confirmation"
          sender() ! unsubscribeText
        }
        else if (response == "update") {
          context.actorSelection("akka://brokerSystem/user/queueManager").tell(updateRequest(response.toString), sender = context.self)
          var updateText = "user update request received"
          sender() ! Write(ByteString(updateText))
        }
        println(s"Data received - ${data.utf8String}")
//        println(s"Data received - ${data.utf8String}")

    }
    case PeerClosed     => context stop self
  }
}

