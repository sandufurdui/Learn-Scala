package broker

import akka.actor.Actor
import akka.io.Tcp
import play.api.libs.json.{JsNull, JsValue, Json}
import scala.collection.mutable.ArrayBuffer

case class sendTopic(topic: String)
case class sendBody(body: String)

class PublisherWorker extends Actor {
  import Tcp._
  val b = new ArrayBuffer[String]()
  println("--------------publisher worker started--------------")
  def receive: Receive = {
//    case s => print(s"printing from publisher worker ----- ${s}")

    case Received(data) =>
      val message = data.utf8String
      if (message.length >50){
        val json: JsValue = Json.parse(message)
        def topic: String = {
          (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
        }
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(sendTopic(topic), sender = context.self)
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(sendBody(message), sender = context.self)

      }


    case PeerClosed     => context stop self
  }
}