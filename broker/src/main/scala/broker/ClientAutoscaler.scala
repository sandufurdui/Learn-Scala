package broker

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
//import broker.ClientWorker

//import scala.collection.mutable.ArrayBuffer

object ClientAutoscaler {
  def props(remote: InetSocketAddress): Props =
    Props(new ClientAutoscaler(remote))
}

class ClientAutoscaler(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, remote)

  println("--------client autoscaler started--------")

  def receive: Receive = {
//    case s => print(s"printing from client autoscaler ----- ${s}")
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
//      queueManager ! response
      //      val b = new ArrayBuffer[String]()
      println(s"Client connected - Remote(client): ${remote.getAddress}:${remote.getPort} Local(sending server): ${local.getAddress}")
      val handler1 = context.actorOf(Props[ClientWorker], name = "clientWorker")
      val connection1 = sender()
      connection1 ! Register(handler1)
  }

}