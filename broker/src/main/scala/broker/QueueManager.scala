package broker

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp
import play.api.libs.json.JsNull
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import scala.collection.mutable.ArrayBuffer

//case class qResponse(y: ArrayBuffer[String])
case class qResponse(y: String)



class QueueManager extends Actor {
  def toInt(s: String): Int = { try { s.toInt } catch { case e: Exception => 0 } }
  println(s"------------Queue manager started------------")

  var count: Int = 0
  var topicsList: String = ""
  var topicsToStore: String = ""
  var topicList: Seq[String] = List[String]()
  var distinctList: Seq[String] = List[String]()

  Message.get(1, "length").map(text => {
    count = toInt(text.body)
  println(s"${count} Messages already stored in db")
  })
  Message.get(0, "messages").map(text => {
    topicsList = text.body
    println(s"Initial topics list: ${topicsList}")
    topicList = topicsList.split(", ")
  })
  val tempList = new ArrayBuffer[String]()
  def receive: Receive = {
    case subscribeRequest(text) =>
      var a = 0
//      sender() ! qResponse(tempList)

      val temp = text
      println(s"sender address ${sender}")
      val test = "asdfdfsfsd"
//      sender().tell("Hello", context.self)
//      sender() ! qResponse(test)
      for( a <- 1 to 900){
//        sender() ! qResponse(tempList)
        Message.get(a, "messages").map(text => {
//          println(s"message topic: ${text.topic}")

          if (temp.contains(text.topic) && (a <= count)){
            println(s"index message ${a}")
//            tempList.+(a.toString)
            tempList.append(a.toString)
//            println(s"temp list: ${tempList}")
          }

        })
//        println(s"${a}")
//        if (a == 900){
//          println(s"mmmmmmm")
//          tempList.foreach( println )
//          println("bbbbbbb")
//          if (tempList.nonEmpty) {
//            println("aaaaaa")
//            for (name <- tempList) {
//              println("llll")
//              sender() ! qResponse(name)
//            }
//          }
//        }
      }


    case getRequest()=>
      if (tempList.nonEmpty) {
        println("aaaaaa")
        for (name <- tempList) {
          println("llll")
          sender() ! qResponse(name)
        }
      }

    case updateRequest(text) =>
      print(s"update request arrived to qmanager ${text}")
      Message.get(0, "messages").map(text => topicsList = text.body)
//      sender ! qResponse(topicsList)
    case message => {
      if (message.toString.length >50) {
        val json: JsValue = Json.parse(message.toString)
        def topic: String = {
          (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
        }
        topicList = topicList :+ topic
        distinctList = topicList.distinct.sorted
        var tempString = distinctList.mkString(", ")
        topicsToStore = tempString.replaceAll("\"", "")
        count = count + 1

        val topicsToSend = Message(0, topicsToStore, null, "messages")
        Message.create(topicsToSend).map(message => print(s""))
        val noOfDbMessages = Message(1, count.toString, null, "length")
        Message.create(noOfDbMessages).map(message => print(s""))
        val toSend = Message(count, message.toString, topic.replaceAll("\"", ""), "messages")
        Message.create(toSend).map(message => print(s""))
        }
      }
    }
}


