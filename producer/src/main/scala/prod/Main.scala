package prod


import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import prod.TcpServer

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 9900
    println(s"Server started! listening to ${host}:${port}")

    val serverProps = TcpServer.props(new InetSocketAddress(host, port))
    val actorSystem: ActorSystem = ActorSystem.create("MyActorSystem")
    val serverActor: ActorRef = actorSystem.actorOf(serverProps)
    serverActor ! ByteString("Starting server...")
  }
}