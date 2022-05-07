package cons

import akka.actor.{ActorRef, ActorSystem, Props}


object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 9900
    println(s"Started client! connecting to ${host}:${port}")

    val actorSystem: ActorSystem = ActorSystem.create("MyActorSystem")

    Thread.sleep(2000)

  }
}
