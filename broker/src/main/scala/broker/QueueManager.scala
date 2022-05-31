package broker

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json._
import play.api.libs.json.JsNull
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsValue


//import scala.collection.mutable.ArrayBuffer
//
//case class JsonMessage(string: String, sender: String = "") {
//  val json: JsValue = Json.parse(string)
//
//  def getMessage: String = {
//    string
//  }
//
//  def get_topic: String = {
//    // Topic is situated in message, tweet, user, time_zone
//    (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
//  }
//
//  def get_field(field: String): JsValue = {
//    (json \ field).get
//  }
//}
//
//case class SubscribeConsumer(consumer_address: String, topic_array: ArrayBuffer[String])
//
//case class UnsubscribeConsumer(consumer_address: String, topic_array: ArrayBuffer[String])
//
//case class CreateListener(port: Int)




import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer

case class qResponse(y: String)

class QueueManager extends Actor {
  println(s"------------Queue manager started------------")

    import Tcp._
    var topicList: Seq[String] = List[String]()
    var distinctList: Seq[String] = List[String]()
    def receive: Receive = {
      case subscribeRequest(text) =>
        print(s"subscribe request arrived to qmanager ${text}")
        sender ! qResponse(distinctList.toString())
      case updateRequest(text) =>
        print(s"update request arrived to qmanager ${text}")
        sender ! qResponse(distinctList.toString())
      case message => {
        if (message.toString.length >50) {
          val json: JsValue = Json.parse(message.toString)

          def topic: String = {
            // Topic is situated in message, tweet, user, time_zone
            (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
          }

          println(s"received topic ${topic}")
          //        val array = Array(1,2,3,2,1,4)
          //        array = array :+ topic
          topicList = topicList :+ topic
          distinctList = topicList.distinct
          //        println(s"(queue manager)received data in queue manager -------- ${message.toString.length}")
          //        b.append(topic)
          //        val b = b.filter(_.contains("apple"))
          //        if (b.exists(p: topic => Boolean)){
          //          b.append("null")
          //        }
          //        b.distinct
          //        println(s"Queue manager buffer ${b.size}")
          //        println(s"Queue manager buffer = ${b}")
          //        println(s"sender adress 1 ---------- ${sender()}")
          //        println(s"sender adress 2 ---------- ${sender}")
          //        println(s"sender adress 3 ---------- ${context.sender}")
          //        println(s"sender adress 4 ---------- ${context.sender()}")


          //        val lolidk = s"buffer stored in queue manager: ${message}"
          //        sender ! qResponse(lolidk)
        }
      }
    }
}
