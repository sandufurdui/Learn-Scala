package broker

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

//case class Start(buf: Array[String]) {
//  override def toString = buf.mkString("->")
//}

class ClientWorker extends Actor {
  import Tcp._
//  val temp = new ArrayBuffer[String]()
  var TCPsender = sender()
  println(s"TCP sender address after declaration ${TCPsender}")
  val b = new ArrayBuffer[String]()
  println(s"------client worker1 started {${context.self}------")
  def receive: Receive = {
    case qResponse(str) => {
      println(s"(client worker) queueManager response ${str}")
//      println(s"sender address ${sender()} should be same as ${TCPsender}")
//      try context.actorSelection(TCPsender).tell(str, sender = context.self)
      TCPsender ! Write(ByteString(s"SERVER_RES: ${str}"))
//      println(s"TCP sender address in response ${TCPsender}")
      //    case PoisonPill ⇒ context
//      val lol = s"lol"
//      sender() ! Write(ByteString(s"SERVER_RES: lol"))
    }
    case Received(data) => {
        var response = data.utf8String

      TCPsender = sender()
      println(s"sender address ${sender()} should be same as ${TCPsender}")
        b.prepend(response)
//      println(s"TCP sender address before assignment ${TCPsender}")
//        TCPsender = sender().toString()
//      println(s"TCP sender address after assignment ${TCPsender}")
        if (response == "subscribe") {
//          sender() ! Write(ByteString("SERVER_RES: ").concat(data))
          var subscribeText = "message from clientWorker"
//          sender() ! Write(ByteString(b.toString()))
          //        print(data)
          val response = data.utf8String
          print(response)
          try context.actorSelection("akka://brokerSystem/user/queueManager").tell(b, sender = context.self)
        }
        else if (response == "unsubscribe") {
          var unsubscribeText = "user unsubscribed confirmation"
          sender() ! Write(ByteString(unsubscribeText))
        }
        println(s"Data received - ${data.utf8String}") //if received smth, prints decoded received data

    }
    case PeerClosed     => context stop self
  }
}

