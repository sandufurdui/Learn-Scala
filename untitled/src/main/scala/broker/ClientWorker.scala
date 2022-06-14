package broker

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import com.google.firebase.tasks.Tasks.await

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.Future
import scala.language.postfixOps

case class subscribeRequest(subString: ArrayBuffer[String], username: String)
case class unsubscribeRequest(y: Seq[String])
case class getTopicsRequest()
case class recoverRequest(name: String)


class ClientWorker extends Actor {
  import Tcp._
  def toInt(s: String): Int = { try { s.toInt } catch { case e: Exception => 0 } }

  var count: Int = 0
  Message.get(1, "messages/length").map(text => {
    count = toInt(text.body)
  })
  var TCPsender: ActorRef = sender()
  var subscribedIDs = new ListBuffer[Int]()
  var finalResponse = new ArrayBuffer[String]()
  var username = ""
  var globalAckKey = 0
  var firstWord = ""
  var lastWord = ""
  var limit = 10
  //   var responseAsSeq = Array[String]
  val responseAsBuff = new ArrayBuffer[String]
  var responseAsList: List[String] = List[String]()
  println(s"------------client worker started ${context.self}------------")
  responseAsBuff.append("f")
  responseAsBuff.append("d")

  def receive: Receive = {

    case qResponse(str) =>
      println(s"(client worker) queueManager response ${str}")
      val r = scala.util.Random
      val keyGenerator = r.nextInt(100)
      globalAckKey = keyGenerator
      val testString = s"{\n    \"key\": ${keyGenerator},\n    \"message\": \"${str}\"\n}"
      TCPsender ! Write(ByteString(testString))

    case Received(data) => {
      TCPsender = sender()
      val responseDecoded = data.utf8String
      val responseAsArray = responseDecoded.split(" ", 2)

      responseAsList= responseDecoded.split(" ").map(_.trim).toList
      val init = responseAsBuff.size
      firstWord = responseAsBuff(init) //the
      lastWord = responseAsBuff(responseAsBuff.size)
      println(s"first ${firstWord}")
      println(s"last ${lastWord}")
      for (i <- responseAsList){
        responseAsBuff.append(i)
        println(s"appended: ${i}")
      }


      if (firstWord == "subscribe") {
        println(s"idk ddddd ${responseAsArray}")

        println(s"username7: ${username}")
        if (responseDecoded.length < 10){
          TCPsender ! Write(ByteString(s"SERVER_RES: please enter at least one topic!"))
        } else{
          context.actorSelection("akka://brokerSystem/user/queueManager").tell(subscribeRequest(responseAsBuff, username), sender = context.self)
          //          Thread.sleep(3000)
          //          for (a <- 1 to count){
          //            Message.get(a, s"toRecover/${username}").map(response => {
          ////              println(s"sss  ${response.id}")
          //              if (subscribedIDs.contains(response.id)){
          //                subscribedIDs.append(response.id)
          //              }
          //              subscribedIDs.distinct
          //              println(s"updated list  ${subscribedIDs.toString}")
          //            })
          //          }
        }
      }
      else if (firstWord == "unsubscribe") {
        //        responseAsBuff.distinct
        println(s"username6: ${username}")
        println(s"last word .${lastWord}.")
        if (responseAsBuff.contains(lastWord)){
          responseAsBuff --= Set(lastWord)
          println(s"unsubscribed from ${lastWord}")
          TCPsender ! Write(ByteString(s"SERVER_RES: unsubscribed"))
        }else {
          TCPsender ! Write(ByteString(s"SERVER_RES: No such topic"))
          println(s"no such topic")
        }
      }
      //        context.actorSelection("akka://brokerSystem/user/queueManager").tell(unsubscribeRequest(lastWord), sender = context.self)

      else if (firstWord == "getT") {
        println(s"username5: ${username}")
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getTopicsRequest(), sender = context.self)
      }
      else if (firstWord == "getM") {
        println(s"username4: ${username}")
        if(subscribedIDs.isEmpty){
          println("queue empty, please subscribe first then update")
        }
        println(s"ids: ${subscribedIDs}")
        println(s"topic List: ${responseAsBuff}")
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getMessagesRequest(subscribedIDs, responseAsBuff, username), sender = context.self)
      }
      else if (firstWord == "update") {
        println(s"username3: ${username}")
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
      else if (responseDecoded.forall(Character.isDigit)) {
        if (responseDecoded.toInt == globalAckKey) {
          println(s"message with key ${globalAckKey} successfully received by client")
        }
        println(s"username2: ${username}")
      }
      else if (firstWord == "name") {
        username = lastWord
      }
      else{
//        if (username.nonEmpty){
          TCPsender ! Write(ByteString("unknown command"))
//        } else {
//          username = data.utf8String
//          println(s"succesfully set name to ${username}")
//          println(s"username1: ${username}")
//          //          for (a <- 1 to count){
//          //            Message.get(a, s"toRecover/${username}").map(response => {
//          ////              println(s"sss  ${response.id}")
//          //              subscribedIDs.append(response.id)
//          //              subscribedIDs.distinct
//          //              println(s"updated list  ${subscribedIDs.toString}")
//          //            })
//          //          }
//        }

      }
      println(s"Data received - ${data.utf8String}")
    }

    case PeerClosed     => context stop self
  }
}

