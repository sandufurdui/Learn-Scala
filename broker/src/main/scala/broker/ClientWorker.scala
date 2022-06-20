package broker

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import play.api.libs.json.{JsNull, JsValue, Json, JsObject}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.language.postfixOps

case class subscribeRequest(subString: ArrayBuffer[String], username: String)
case class unsubscribeRequest(y: Seq[String])
case class getTopicsRequest()
case class recoverRequest(name: String)
case class ToClient(response: String)

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
   val idList = new ArrayBuffer[Int]()
   val responseAsBuff = new ArrayBuffer[String]
   var responseAsList: List[String] = List[String]()
  println(s"------------client worker started ${context.self}------------")

  def receive: Receive = {
    case qResponse(str) =>
      println(s"(client worker) queueManager response ${str}")
      val r = scala.util.Random
      val keyGenerator = r.nextInt(1000)
      globalAckKey = keyGenerator
      val testString = s"{\n    \"key\": ${keyGenerator},\n    \"message\": \"${str}\"\n}"
      idList.append(keyGenerator)
        TCPsender ! Write(ByteString(testString))

    case Received(data) => {
      val dataDecoded = data.utf8String
      val json: JsValue = Json.parse(dataDecoded)
      val command = (json \ "command").getOrElse(JsNull).toString().replaceAll("\"", "")
      val mode = (json \ "mode").getOrElse(JsNull).toString().replaceAll("\"", "")
      val message = (json \ "postMessage").getOrElse(JsNull).toString()
      // println(s"commmand from client: ${command}")
      if (username.isEmpty){
         username = (json \ "username").getOrElse(JsNull).toString().replaceAll("\"", "")
      }

      // println(s"username from client: ${username}")
      TCPsender = sender()

      if (mode == "get" ){
      println("user in get mode")
      val responseDecoded = command.replaceAll("\"", "")
      responseAsList= responseDecoded.split(" ").map(_.trim).toList

      firstWord = responseAsList.head
      lastWord = responseAsList.last
      if (firstWord == "subscribe") {
        for (i <- responseAsList){
          responseAsBuff.append(i)
          responseAsBuff.distinct
        }
        if (responseDecoded.length < 10){
          TCPsender ! Write(ByteString(s"SERVER_RES: please enter at least one topic!"))
        } else{
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(subscribeRequest(responseAsBuff, username), sender = context.self)
        }
      }
      else if (firstWord == "unsubscribe") {
        if (responseAsBuff.contains(lastWord)){
          responseAsBuff --= Set(lastWord)
          println(s"unsubscribed from ${lastWord}")
          TCPsender ! Write(ByteString(s"SERVER_RES: unsubscribed from ${lastWord}"))
          for (id <- subscribedIDs){
            Message.get(id, s"toRecover/${username}").map(response => {
              if (response.topic == lastWord){
                val topicsToSend = Message(id, null, null, s"toRecover/${username}")
                Message.create(topicsToSend).map(message => print(s""))
                subscribedIDs --= Set(id)
              }
            })
          }
        }else {
          TCPsender ! Write(ByteString(s"SERVER_RES: No such topic"))
          println(s"no such topic")
        }
      }
      else if (lastWord == "topics" && firstWord == "get") {
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getTopicsRequest(), sender = context.self)
      }
      else if (lastWord == "messages" && firstWord == "get") {
         if(subscribedIDs.isEmpty){
           println("queue empty, please subscribe first the update")
        }
        for (id <- subscribedIDs) {
          val r = scala.util.Random
          val keyGenerator = r.nextInt(1000)
          globalAckKey = keyGenerator
          val testString = s"{\n    \"key\": ${keyGenerator},\n    \"message\": \"${id}\"\n}"
          TCPsender ! Write(ByteString(testString))
          Thread.sleep(1000)
          idList.append(keyGenerator)
        }
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(getMessagesRequest(subscribedIDs, responseAsBuff, username), context.self)
      }
      else if (firstWord == "update") {
        for (a <- 1 to count){
          Message.get(a, s"toRecover/${username}").map(response => {
            if (!subscribedIDs.contains(response.id) && response.topic.nonEmpty){
              subscribedIDs.append(response.id)
            }
            subscribedIDs.distinct
            println(s"updated list ${subscribedIDs.toString}")
            if (a == count || a == count-1){
              println("update finished")
            }
          })
        }
      }
      else if (data.utf8String.forall(Character.isDigit)) {
        if (idList.contains(data.utf8String.toInt)) {
          println(s"client received message with id: ${data.utf8String.toInt}")
          idList --= Seq(data.utf8String.toInt)
        }
      }
      else{
        if (username.nonEmpty){
          TCPsender ! Write(ByteString("unknown command"))
        } else {
          // username = data.utf8String
         
          for (a <- 1 to count){
            Message.get(a, s"toRecover/${username}").map(response => {
              subscribedIDs.append(response.id)
              subscribedIDs.distinct
              println(s"updated list  ${subscribedIDs.toString}")
            })
          }
        }

      }
    } else if (mode == "post" && message.nonEmpty){
      println("user in post mode")
      val json: JsValue = Json.parse(message)
        def topic: String = {
          (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
        }
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(sendTopic(topic), sender = context.self)
        context.actorSelection("akka://brokerSystem/user/queueManager").tell(sendBody(message), sender = context.self)
    }
      println(s"Data received - ${data.utf8String}")
    }

    case PeerClosed     => context stop self
  }
}

