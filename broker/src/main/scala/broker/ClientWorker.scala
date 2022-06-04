package broker

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

case class subscribeRequest(y: Seq[String])
case class updateRequest(y: String)
case class getRequest()

case class sendToClient(y: String, key: Int)

//case class Start(buf: Array[String]) {
//  override def toString = buf.mkString("->")
//}

class ClientWorker extends Actor {
  import Tcp._

  var TCPsender: ActorRef = sender()
  val b = new ArrayBuffer[String]()
//  var resp: Array[String] = Array[String]()
  var resp = List[String]()
  println(s"------------client worker started ${context.self}------------")
  def receive: Receive = {
    case qResponse(str) => {
      println(s"(client worker) queueManager response ${str}")
//      for( a <- 1 to 30){
//        TCPsender ! Write(ByteString(s"SERVER_RES: ${a}"))
//      }
      TCPsender ! Write(ByteString(s"SERVER_RES: ${str}"))
    }
    case Received(data) => {
      var response = data.utf8String
      var firstWord = response.split(" ").head
//      println(s"first word of the command ${toCheck}")
      TCPsender = sender()
      b.prepend(response)
      if (firstWord == "subscribe") {
        val response = data.utf8String
        val resp = response.split(" ")
//        TCPsender ! Write(ByteString(s"SERVER_RES: afdfsd"))
//        println(s"---------${resp}")
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(subscribeRequest(resp), sender = context.self)
      }
      else if (response == "unsubscribe") {
        var unsubscribeText = "user unsubscribed confirmation"
        TCPsender ! unsubscribeText
      }
      else if (response == "update") {
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(updateRequest(response.toString), sender = context.self)
        var updateText = "user update request received"
        TCPsender  ! Write(ByteString(updateText))
      }
      else if (response == "get") {
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getRequest(), sender = context.self)
//        var updateText = "user update request received"
//        TCPsender  ! Write(ByteString(updateText))
      }
      println(s"Data received - ${data.utf8String}")

    }
    case PeerClosed     => context stop self
  }
}

