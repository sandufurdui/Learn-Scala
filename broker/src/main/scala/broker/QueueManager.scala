package broker

import akka.actor.{Actor, ActorSystem, Props}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

case class qResponse(response: String)
case class subscribedTopics(y: Array[Int])
case class getMessagesRequest(idList: ListBuffer[Int], topicList: Seq[String], username: String)

class QueueManager extends Actor {
  def toInt(s: String): Int = { try { s.toInt } catch { case e: Exception => 0 } }
  println(s"------------Queue manager started------------")

  var count: Int = 0
  var topicsList: String = ""
  var topicsToStore: String = ""
  var topicList: Seq[String] = List[String]()
  var distinctList: Seq[String] = List[String]()

  Message.get(1, "messages/length").map(text => {
    count = toInt(text.body)
  println(s"${count} Messages already stored in db")
  })
  Message.get(0, "messages/topicList").map(text => {
    topicsList = text.body
    println(s"Stored topics: ${topicsList}")
    topicList = topicsList.split(", ")
  })

  def receive: Receive = {
    case sendTopic(topic) =>
      count = count + 1
      println(s"received topic in queue: ${topic}")
      topicList = topicList :+ topic
      distinctList = topicList.distinct.sorted
      var tempString = distinctList.mkString(", ")
      topicsToStore = tempString.replaceAll("\"", "")

      val topicsToSend = Message(0, topicsToStore, null, "messages/topicList")
      Message.create(topicsToSend).map(message => print(s""))
      val topicToSend = Message(count, null, topic.replaceAll("\"", ""), "messages/topicList")
      Message.create(topicToSend).map(message => print(s""))
      val length = Message(1, count.toString, null, "messages/length")
      Message.create(length).map(message => print(s""))

    case sendBody(body) =>
      println(s"received topic in queue: ${body.length}")
      val topicsToSend = Message(count, body, null, "messages/body")
      Message.create(topicsToSend).map(message => print(s""))

    case subscribeRequest(text, name) =>
      for (a <- 1 to count) {
        Message.get(a, "messages/topicList").map(response => {
          if (text.contains(response.topic) && (a <= count)) {
            Message.get(a, "messages/body").map(response1 => {
              val topicsToSend = Message(a, response1.body, response.topic, s"toRecover/${name}")
              Message.create(topicsToSend).map(message => print(s""))
            })

          }
        })
      }

    case getMessagesRequest(idList, topicList, username) =>
      for (id <- idList) {
        Message.get(id, s"toRecover/${username}").map(text => {
//          if(topicList.contains(text.topic)){
            println(s"id: ${text.id}")
            println(s"topic: ${text.topic}")
//          }
        })
      }

    case getTopicsRequest() =>
      Message.get(0, "messages/topicList").map(text => topicsList = text.body)
      sender ! qResponse(topicsList)

    case recoverRequest(name) =>
      println(name)

  }
}


