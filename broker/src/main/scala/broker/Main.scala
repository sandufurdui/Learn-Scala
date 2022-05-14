package broker

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import broker.TCPServer

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 5600
    println(s"Server started! listening to ${host}:${port}")

    val serverProps = TCPServer.props(new InetSocketAddress(host, port))
    val actorSystem: ActorSystem = ActorSystem.create("ProducersActorSystem")
    val serverActor: ActorRef = actorSystem.actorOf(serverProps)
    serverActor ! ByteString("Starting server...")
  }
}
