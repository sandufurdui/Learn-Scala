package broker

import akka.actor.Actor

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}
case class qResponse(response: String)
case class subscribedTopics(y: Array[Int])
case class getMessagesRequest(idList: ListBuffer[Int], topicList: ArrayBuffer[String], username: String)

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
      println(s"array received in subscribe request ${text}")
      for (a <- 1 to count) {
        Message.get(a, "messages/topicList").map(response => {
          if (text.contains(response.topic) && (a <= count)) {
            Message.get(a, "messages/body").map(response1 => {
              val topicsToSend = Message(a, response1.body, response.topic, s"toRecover/${name}")
              Message.create(topicsToSend).map(message => print(s""))
              println(s"new message ${response.id}")
            })

          }
        })
      }

    case getMessagesRequest(idList, topicList, username) =>
      var test = new ArrayBuffer[String]()
      val f = Future {
        Thread.sleep(Random.nextInt(500))
        for (id <- idList) {
          Message.get(id, s"toRecover/${username}").map(text => {
            if(topicList.contains(text.topic)){
              println(s"id: ${text.id}")
              println(s"topic: ${text.topic}")
              test.append(text.id.toString)
            }
          })
        }
      }

      f.onComplete {
//        case Success(value) => sender() ! qResponse(test.toString())
        case Failure(e) => e.printStackTrace
        case Success(value) => println(s"finished getting the messages ${test.toString()}")
      }


    case getTopicsRequest() =>
      Message.get(0, "messages/topicList").map(text => topicsList = text.body)
      sender ! qResponse(topicsList)

    case recoverRequest(name) =>
      println(name)

  }
}


