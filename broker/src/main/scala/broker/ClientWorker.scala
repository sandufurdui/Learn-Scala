package broker

import akka.actor.Actor
import akka.io.Tcp
import akka.util.ByteString
import scala.collection.mutable.ArrayBuffer
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
//case class Success(actorRef: Any)
//case class Failure(ex: Any)
//import akka._

case class Start(buf: Array[String]) {
  override def toString = buf.mkString("->")
}

class ClientWorker extends Actor {
  import Tcp._
  val b = new ArrayBuffer[String]()
//  println("--------client worker started--------")
  def receive: Receive = {
//    val actorName = self.path.name
//    case s => print(s"printing from client worker ----- ${s}")
//      println("LocalActor got: " + s)
//      val msg = "test message from client worker"
//      context.actorSelection("/user/brokerSystem/queueManager").tell(msg, sender)
//      context.actorSelection("/brokerSystem/queueManager").tell
//      context.ac
    case Received(data) =>
//      akka.system.actorSelection("user/" + "somename").resolveOne().onComplete {
//        case Success(actorRef) => // logic with the actorRef
//        case Failure(ex) => println("user/" + "somename" + " does not exist")
//      }
//      val actorName = self.path.name
//      println(self.path.name)
//      implicit val timeout = Timeout(5 seconds)
////      val future = myActor ? AskNameMessage
//      val result = Await.result(context.actorSelection("akka://brokerSystem/user/queueManager"), timeout.duration).asInstanceOf[String]
//      println(result)

      var response = data.utf8String
      try context.actorSelection("akka://brokerSystem/user/queueManager").tell(response, sender)

      b.prepend(response)
//      queueManager ! response
      //      b = b + response
      //      b.insert(0, "ffff ")

      if (response == "subscribe") {
        sender() ! Write(ByteString("SERVER_RES: ").concat(data))
        var subscribeText = "user subscribed confirmation"
        //        b.map(A)
        //        b.toString()
        sender() ! Write(ByteString(b.toString()))
        //        sender() ! Write(ByteString(response))
      }
      else if (response == "unsubscribe") {
        var subscribeText = "user unsubscribed confirmation"
        sender() ! Write(ByteString(subscribeText))
      }
      //      if (response == "subscribe") {
      //        var subscribeText = "user subscribed confirmation"
      //        sender() ! Write(ByteString(subscribeText))
      //        sender() ! Write(ByteString(response))
      //      }
      //      else {
      //        //        var subscribeText = "closing connectioon"
      //        //        sender() ! Write(ByteString(subscribeText))
      //        //        context stop self

      //        println(b.size)
      //      }
      println(s"Data received - ${data.utf8String}")  //if received smth, prints decoded received data
    //sends back
    case PeerClosed     => context stop self
  }
}

