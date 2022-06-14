package broker

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
import akka.util.ByteString
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

  println(s"------------Client autoscaler started------------")

  def receive: Receive = {
    //    case s => print(s"printing from client autoscaler ----- ${s}")
    case b @ Bound(localAddress) =>
    //      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
      //      queueManager ! response
      //      val b = new ArrayBuffer[String]()
      println(s"Client connected - Remote(client): ${remote.getAddress}:${remote.getPort}")
      val handler1 = context.actorOf(Props[ClientWorker])
      val connection1 = sender()
      connection1 ! Register(handler1)
      connection1 ! Write(ByteString(s"SERVER_RES: Client connected to server \nUse commands bellow to make requests\n - subscribe <topicA> <topicB> ... -- to subscribe to specified topics, use it to add more topics to the subscribed list\n - unsubscribe <topic> \n - update -- to update topic list \n - getT -- to get existent topics \n - getM -- to get messages for the subscribed topics - recover <client_name> -- to try recover from last point"))
  }

}