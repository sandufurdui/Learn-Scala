package prod

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
import prod.SimplisticHandler

object TcpServer {
  def props(remote: InetSocketAddress): Props =
    Props(new TcpServer(remote))
}

class TcpServer(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, remote)

  def receive: Receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
      println(s"Client connected - Remote(Client): ${remote.getAddress} Local(Server): ${local.getAddress}")
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
  }

}
