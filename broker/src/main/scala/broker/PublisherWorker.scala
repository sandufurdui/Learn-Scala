package broker

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer

class PublisherWorker extends Actor {
  import Tcp._
  val b = new ArrayBuffer[String]()
  println("--------------publisher worker started--------------")
  def receive: Receive = {
//    case s => print(s"printing from publisher worker ----- ${s}")
    case Received(data) =>
      var response = data.utf8String
//      println(response)
      try context.actorSelection("akka://brokerSystem/user/queueManager").tell(response, sender = context.self)
//      b.prepend(response)
//      println(b.size)
      //      }
//      println(s"Data received - ${data.utf8String}")  //if received smth, prints decoded received data
    //sends back
    case PeerClosed     => context stop self
  }
}