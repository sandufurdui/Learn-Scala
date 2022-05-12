package cons

import java.net.InetSocketAddress
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import cons.TcpClient
import ujson.IndexedValue.True

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 5600
    println(s"Started client! connecting to ${host}:${port}")

    val clientProps = Props(classOf[TcpClient], new InetSocketAddress(host, port), null)

    val actorSystem: ActorSystem = ActorSystem.create("ConsumersActorSystem")
    val clientActor: ActorRef = actorSystem.actorOf(clientProps)

    Thread.sleep(2000)
    while (true){
      val input = scala.io.StdIn.readLine("command> ")
      if (input != null){
        clientActor ! ByteString(input)
      }
    }

  }
}
