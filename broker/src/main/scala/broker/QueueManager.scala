package broker

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json._
import play.api.libs.json.JsNull
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsValue


import scala.concurrent.Future


import scala.beans.BeanProperty
//import com.twitter.util.{Future, Promise}
import com.google.firebase.database._



import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer

case class qResponse(y: String)

class QueueManager extends Actor {
  println(s"------------Queue manager started------------")

    import Tcp._
    var count :Int = 1711
    var temp :String = ""
    var topicList: Seq[String] = List[String]()
    var distinctList: Seq[String] = List[String]()
    def receive: Receive = {
      case subscribeRequest(text) =>
//        print(s"subscribe request arrived to qmanager ${text}")
        Message.get(0, "messages").map(text => temp = text.toString)
        sender ! qResponse(temp)
//        val toSend = Message(1, "message.toString", "topic", "messages")
//        val lol = Message(1, "message.toString", "topic", "urmom")
//        Message.create(toSend).map(message => println(s"Created message with id ${message.id}"))
//        Message.create(lol).map(message => println(s"Created message with id ${message.id}"))
//        Message.get(1, "messages").map(message => println(s"message stored in db= ${message}"))
//        Message.get(1, "urmom").map(message => println(s"message dynamically stored in db= ${message}"))

      case updateRequest(text) =>
        print(s"update request arrived to qmanager ${text}")
        sender ! qResponse(distinctList.toString())
      case message => {
        if (message.toString.length >50) {
          val json: JsValue = Json.parse(message.toString)

          def topic: String = {
            (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
          }

//          println(s"queue length ${}")
          topicList = topicList :+ topic
          distinctList = topicList.distinct.sorted
//          println(s"queue length ${distinctList.size}")
          count = count + 1
          val topicsToSend = Message(0, distinctList.toString, null, "messages")
          Message.create(topicsToSend).map(message => println(s"updated topics in db ${distinctList.size}"))
          val toSend = Message(count, message.toString, topic, "messages")
          Message.create(toSend).map(message => println(s"Created message with id ${message.id}"))
//          Message.get(1, "urmom").map(message => println(s"message dynamically stored in db= ${message}"))

//          val tim = Message(1, message.toString, topic)
//          Message.create(tim).map(message => println(s"Created message with id ${message.id}"))



        }
      }
    }
}


