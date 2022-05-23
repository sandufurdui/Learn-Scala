//
////        PublisherAutoscaler(act) -> PublisherWorker(act)
////       /                            | (topics flow)
////      /                             v
////Main  - QueueManager(act)  -> ArrayBuffer(of topics)
////      \                             | (subscribed topics flow only)
////       \                            v
////        ClientAutoscaler(act) -> ClientWorker(act)
//
//
package broker

import java.net.InetSocketAddress
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
case object StartMessage
//import broker.PublisherAutoscaler
//import broker.ClientAutoscaler

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val receivePort = 5600
    val sendPort = 5601
    println(s"Server started! listening to ${host}:${receivePort}")
    println(s"Server started! listening to ${host}:${sendPort}")
    println(s"Queue manager actor started!")

//    val system = ActorSystem("PingPongSystem")
//    val pong = system.actorOf(Props[Pong], name = "pong")
//    val ping = system.actorOf(Props(new Ping(pong)), name = "ping")


    val system = ActorSystem("brokerSystem")
    val qManager = system.actorOf(Props[QueueManager], name="queueManager")
//    val PublisherActor = system.actorOf(Props[PublisherAutoscaler], name="publisherAutoscaler")
//    val ClientActor = system.actorOf(Props[ClientAutoscaler], name="clientAutoscaler")

    val PublisherProps = PublisherAutoscaler.props(new InetSocketAddress(host, receivePort))
    val ClientProps = ClientAutoscaler.props(new InetSocketAddress(host, sendPort))
//    val publisherActorSystem: ActorSystem = ActorSystem.create("PublisherActorSystem")
//    val clientActorSystem: ActorSystem = ActorSystem.create("ClientActorSystem")
    val PublisherActor: ActorRef = system.actorOf(PublisherProps, name = "publisher")
    val ClientActor: ActorRef = system.actorOf(ClientProps, name= "client")
//    PublisherActor ! ByteString("Starting receiving server...")
//    ClientActor ! ByteString("Starting sending server...")
    PublisherActor ! StartMessage
    ClientActor ! StartMessage
//    qManager ! StartMessage

  }
}



//package broker
//import akka.actor._
//
//case object PingMessage
//case object PongMessage
//case object StartMessage
//case object StopMessage
//
//class Ping(pong: ActorRef) extends Actor {
//  var count = 0
//  def incrementAndPrint: Unit = { count += 1; println("ping") }
//  def receive: Receive = {
//    case StartMessage =>
//      incrementAndPrint
//      pong ! PingMessage
//    case PongMessage =>
//      incrementAndPrint
//      if (count > 99) {
//        sender ! StopMessage
//        println("ping stopped")
//        context.stop(self)
//      } else {
//        sender ! PingMessage
//      }
//    case _ => println("Ping got something unexpected.")
//  }
//}
//
//class Pong extends Actor {
//  def receive: Receive = {
//    case PingMessage =>
//      println(" pong")
//      sender ! PongMessage
//    case StopMessage =>
//      println("pong stopped")
//      context.stop(self)
//    case _ => println("Pong got something unexpected.")
//  }
//}
//
//object PingPongTest extends App {
//  val system = ActorSystem("PingPongSystem")
//  val pong = system.actorOf(Props[Pong], name = "pong")
//  val ping = system.actorOf(Props(new Ping(pong)), name = "ping")
//  // start the action
//  ping ! StartMessage
//  // commented-out so you can see all the output
//  //system.shutdown
//}