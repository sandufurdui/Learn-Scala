package broker

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import com.google.firebase.tasks.Tasks.await

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.Future
import scala.language.postfixOps

case class subscribeRequest(subString: Seq[String], username: String)
case class unsubscribeRequest(y: Seq[String])
case class getTopicsRequest()
case class recoverRequest(name: String)


 class ClientWorker extends Actor {
  import Tcp._
  def toInt(s: String): Int = { try { s.toInt } catch { case e: Exception => 0 } }
//  def async[T](body: => T): Future[T]
//  def await[T](future: Future[T]): T

  var count: Int = 0
  Message.get(1, "messages/length").map(text => {
    count = toInt(text.body)
  })
  var TCPsender: ActorRef = sender()
  var subscribedIDs = new ListBuffer[Int]()
  var finalResponse = new ArrayBuffer[String]()
  var username = ""
  println(s"------------client worker started ${context.self}------------")

  def receive: Receive = {
    case qResponse(str) =>
      println(s"(client worker) queueManager response ${str}")
      TCPsender ! Write(ByteString(s"SERVER_RES: ${str}"))

    case Received(data) => {
      var response = data.utf8String
      var firstWord = response.split(" ").head
      var lastWord = response.split(" ").tail
      TCPsender = sender()
      val responseDecoded = data.utf8String
      val responseAsSeq = responseDecoded.split(" ")
      if (firstWord == "subscribe") {
        if (responseDecoded.length < 10){
          TCPsender ! Write(ByteString(s"SERVER_RES: please enter at least one topic!"))
        } else{
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(subscribeRequest(responseAsSeq, username), sender = context.self)
          Thread.sleep(3000)
          for (a <- 1 to count){
            Message.get(a, s"toRecover/${username}").map(response => {
//              println(s"sss  ${response.id}")
              if (!subscribedIDs.contains(response.id)){
                subscribedIDs.append(response.id)
              }
              subscribedIDs.distinct
              println(s"updated list  ${subscribedIDs.toString}")
            })
          }
        }
      }
      else if (firstWord == "unsubscribe") {
//        if(responseAsSeq.contains(lastWord)){
//          var k = responseAsSeq.indexOf(lastWord)
//          println(s"index ${k}")
//          println(s"before ${responseAsSeq.toString}")
//          responseAsSeq.slice(k, k)
//          println(s"after ${responseAsSeq.toString}")
//          (1, responseAsSeq)
//        } else{
//          println(s"trying to reach ${lastWord.mkString("Array(", ", ", ")").replace("Array(", "").replace(")","")}")
////          println(s"trying to reach 2 ${lastWord.toString}")
//          println(s"no such topic!")
//          println(s"command ${responseAsSeq.toString}")
//        }
      }
//        context.actorSelection("akka://brokerSystem/user/queueManager").tell(unsubscribeRequest(lastWord), sender = context.self)

      else if (response == "getT") {
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getTopicsRequest(), sender = context.self)
      }
      else if (response == "getM") {
         if(subscribedIDs.isEmpty){
           println("queue empty, please subscribe first then update")
        }
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getMessagesRequest(subscribedIDs, responseAsSeq, username), sender = context.self)
      }
      else if (firstWord == "update") {
        for (a <- 1 to count){
          Message.get(a, s"toRecover/${username}").map(response => {
//            println(s"sss  ${response.id}")
            if (!subscribedIDs.contains(response.id)){
              subscribedIDs.append(response.id)
            }
            subscribedIDs.distinct
            println(s"updated list  ${subscribedIDs.toString}")
          })
        }
      }
      else if (firstWord == "recover") {
        var testString = "{\n    \"key\": 12,\n    \"message\": \"test message\"\n}"
        TCPsender ! Write(ByteString(testString))
//        if(subscribedIDs.isEmpty){
//          for (a <- 1 to count){
//            Message.get(a, s"toRecover/${username}").map(response => {
//              println(s"sss  ${response.id}")
//              subscribedIDs.append(response.id)
//              println(s"ssfdsfss  ${subscribedIDs.toString}")
//            })
//          }
//        }
//        context.actorSelection("akka://brokerSystem/user/queueManager").tell(recoverRequest(lastWord.toString), sender = context.self)
      }
      else{
        if (username.length != 0){
          TCPsender ! Write(ByteString("unknown command"))
        } else {
          username = data.utf8String
          println(s"succesfully set name to ${username}")
          for (a <- 1 to count){
            Message.get(a, s"toRecover/${username}").map(response => {
//              println(s"sss  ${response.id}")
              subscribedIDs.append(response.id)
              subscribedIDs.distinct
              println(s"updated list  ${subscribedIDs.toString}")
            })
          }
        }

      }
      println(s"Data received - ${data.utf8String}")
    }

    case PeerClosed     => context stop self
  }
}

