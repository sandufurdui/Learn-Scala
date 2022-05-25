package broker

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer

case class qResponse(y: String)

class QueueManager extends Actor {
  println(s"------Queue manager started------")

    import Tcp._
    val b = new ArrayBuffer[String]()
    def receive: Receive = {
      case message => {
        println(s"(queue manager)received data in queue manager -------- ${message}")
        b.append(message.toString)
//        println(s"Queue manager buffer = ${b}")
//        println(s"sender adress 1 ---------- ${sender()}")
//        println(s"sender adress 2 ---------- ${sender}")
//        println(s"sender adress 3 ---------- ${context.sender}")
//        println(s"sender adress 4 ---------- ${context.sender()}")


        val lolidk = s"buffer stored in queue manager: ${message}"
        sender ! qResponse(lolidk)
      }
    }
}
