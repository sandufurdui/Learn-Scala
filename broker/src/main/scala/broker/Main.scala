package broker

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
//import broker.TCPServer
import broker.TCPServer1
//import broker.

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val receivePort = 5600
    val sendPort = 5601
    println(s"Server started! listening to ${host}:${receivePort}")
    println(s"Server started! listening to ${host}:${sendPort}")

    val serverProps = TCPServer.props(new InetSocketAddress(host, receivePort))
    val serverProps1 = TCPServer1.props(new InetSocketAddress(host, sendPort))
    val actorSystem: ActorSystem = ActorSystem.create("ProducersActorSystem")
    val actorSystem1: ActorSystem = ActorSystem.create("ProducersActorSystem1")
    val serverActor: ActorRef = actorSystem.actorOf(serverProps)
    val serverActor1: ActorRef = actorSystem1.actorOf(serverProps1)
    serverActor ! ByteString("Starting receiving server...")
    serverActor1 ! ByteString("Starting sending server...")
  }
}
