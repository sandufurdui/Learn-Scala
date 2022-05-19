package broker

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
import broker.SimplisticHandler1

import scala.collection.mutable.ArrayBuffer

object TCPServer1 {
  def props(remote: InetSocketAddress): Props =
    Props(new TCPServer1(remote))
}

class TCPServer1(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, remote)

  def receive: Receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
      //      val b = new ArrayBuffer[String]()
      println(s"Client connected - Remote(client): ${remote.getAddress}:${remote.getPort} Local(sending server): ${local.getAddress}")
      val handler1 = context.actorOf(Props[SimplisticHandler1])
      val connection1 = sender()
      connection1 ! Register(handler1)
  }

}
