package broker

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
import broker.SimplisticHandler

import scala.collection.mutable.ArrayBuffer

object TCPServer {
  def props(remote: InetSocketAddress): Props =
    Props(new TCPServer(remote))
}

class TCPServer(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, remote)

  def receive: Receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
//      val b = new ArrayBuffer[String]()
      println(s"Client connected - Remote(publisher): ${remote.getAddress}:${remote.getPort} Local(receiving server): ${local.getAddress}")
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection1 = sender()
      connection1 ! Register(handler)
  }

}
