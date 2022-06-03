package broker

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.actor.Props
import broker.PublisherWorker

import scala.collection.mutable.ArrayBuffer

object PublisherAutoscaler {
  def props(remote: InetSocketAddress): Props =
    Props(new PublisherAutoscaler(remote))
}

class PublisherAutoscaler(remote: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  println(s"------------Publisher autoscaler started------------")

  IO(Tcp) ! Bind(self, remote)
//  val msg = "test message from publisher worker"
//  try context.actorSelection("akka://brokerSystem/user/queueManager").tell(msg, sender)
//  try context.actorSelection("akka://brokerSystem/user/client").tell(msg, sender)
//  try context.actorSelection("akka://brokerSystem/user/publisher/publisherWorker").tell(msg, sender)
//  try context.actorSelection("akka://brokerSystem/user/client/clientWorker").tell(msg, sender)
//  try context.actorSelection("akka://brokerSystem/user/client").tell(msg, sender)
//  catch print(error)

  def receive: Receive = {
//    case s => print(s"printing from publisher autoscaler ----- ${s}")
    case b @ Bound(localAddress) =>
//      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) =>
      //      val b = new ArrayBuffer[String]()
      println(s"Client connected - Remote(publisher): ${remote.getAddress}:${remote.getPort} Local(receiving server): ${local.getAddress}")
      val handler = context.actorOf(Props[PublisherWorker])
      val connection1 = sender()
      connection1 ! Register(handler)
  }

}