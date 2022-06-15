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

    val system = ActorSystem("brokerSystem")
    val qManager = system.actorOf(Props[QueueManager], name="queueManager")

    val PublisherProps = PublisherAutoscaler.props(new InetSocketAddress(host, receivePort))
    val ClientProps = ClientAutoscaler.props(new InetSocketAddress(host, sendPort))
    val PublisherActor: ActorRef = system.actorOf(PublisherProps, name = "publisher")
    val ClientActor: ActorRef = system.actorOf(ClientProps, name= "client")
  }
}
