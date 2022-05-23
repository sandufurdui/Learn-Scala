package broker

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer



class QueueManager extends Actor {
    import Tcp._
    val b = new ArrayBuffer[String]()
//    val system = ActorSystem("brokerSystem")
//    val actor: Not actorSystem.actorOf(Props(ActorExample), "RootActor")
   // val test = system.actorOf(Props[QueueManager], "client")
    def receive: Receive = {
      case s => {
//        b.append(s)
        val k = "lol"
        b.append(k)
//        println(s"received data in queue manager -------- ${s}")
        println(s"Queue manager buffer size = ${b.size}")
//        sender() ! "queue manager received message"
      }
//      case StartMessage =>
////        println(context.actorSelection("/user/brokerSystem/"))
////        println(s"queue manager name ${self.path.name}")
//////        val msg = "test message from client worker"
//////        context.actorSelection("/brokerSystem/queueManager").tell(msg, sender)
////      println("lol")
    }
}
