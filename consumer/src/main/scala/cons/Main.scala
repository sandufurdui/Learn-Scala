package cons

import java.net.InetSocketAddress
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import cons.TcpClient
import ujson.IndexedValue.True

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 5601
    println("-----------------------------------------------")
    println(s"Started client! connecting to ${host}:${port}")
    println("-----using mode(post/get). to change the mode use command \"mode <post>\" or \"mode <get>\".-----")
    println("-----------------------------------------------")

    var username = scala.io.StdIn.readLine(s"choose a username: ")
    if (username.isEmpty){
      username = scala.io.StdIn.readLine(s"Please try again: ")
      // println(s"name successfully set to ${username}")
    }
    if (username.nonEmpty){
      //  username = scala.io.StdIn.readLine(s"Please try again: ")
      println(s"name successfully set to ${username}")
    }
    val clientProps = TcpClient.props(new InetSocketAddress(host, port), null)
    var mode = "get"
    val actorSystem: ActorSystem = ActorSystem.create("ConsumersActorSystem")
    val clientActor: ActorRef = actorSystem.actorOf(clientProps)
    var postMessage = ""
    var postCommand = ""
    // println(s"SERVER_RES: Client connected to server \nUse commands bellow to make requests\n - subscribe <topicA> <topicB> ... -- to subscribe to specified topics, use it to add more topics to the subscribed list\n - unsubscribe <topic> \n - update -- to update \n - get topics -- to get existent topics \n - get messages -- to get messages for the subscribed topics")
    
    Thread.sleep(2000)
    while (true){
      
      var responseAsList: List[String] = List[String]()
      val r = scala.util.Random
      // println(s"current mode is: ${mode}")
      val input = scala.io.StdIn.readLine(s"mode ${mode}> ")
      if (input != null){
        responseAsList= input.split(" ").map(_.trim).toList
        // println(s"client input: ${responseAsList}")
        if (responseAsList.head == "post" && mode == "post"){
          val topic = scala.io.StdIn.readLine(s"enter the timezone")
          val name = scala.io.StdIn.readLine(s"enter the screen name")
          val keyGenerator = r.nextInt(1000)
          val temp = s"{ \"message\": { \"tweet\": { \"user\": { \"time_zone\": \"${topic}\", \"screen_name\": \"${name}\" } } } }"
          val testString = s"\n{\n    \"key\": ${keyGenerator},\n    \"postMessage\": ${temp},\n    \"command\": \"${input}\", \n    \"mode\": \"${mode}\",\n    \"username\": \"${username}\"\n}"
          clientActor ! ByteString(testString)
//          val temp = s"\n{ \"message\": { \"tweet\": { \"user\": { \"time_zone\": \"${topic}\", screen_name: \"${name}\" } } } }"
//          clientActor ! ByteString(temp)
        }


        if (responseAsList.head == "mode"){
          // println(s"currentm mode is: ${mode}")


          if (responseAsList.last == "post"){
            if(responseAsList.last == mode ){
            println(s"mode is already: ${mode}")
          }else{
            mode = "post"
            println(s"mode changed to: ${mode}")
         }}
         else if (responseAsList.last == "get" ){
          if(responseAsList.last == mode ){
            println(s"mode is already: ${mode}")
          }else{
            val keyGenerator = r.nextInt(1000)
            mode = "get"
            println(s"mode changed to: ${mode}")
            // val testString = s"\n{\n    \"key\": ${keyGenerator},\n    \"postMessage\": ${postMessage},\n    \"command\": \"${input}\", \n    \"mode\": \"${mode}\",\n    \"username\": \"${username}\"\n}"
            // clientActor ! ByteString(testString)
          }
          }
          else {
            println("no such mode! \neither use <post> to post, either <get> to get messages")
          }
        }
        if(responseAsList.head == "get" || responseAsList.head == "subscribe" || responseAsList.head == "unsubscribe" || responseAsList.head == "update") {
          val keyGenerator = r.nextInt(1000)
          val testString = s"\n{\n    \"key\": ${keyGenerator},\n    \"postMessage\": \"${postMessage}\",\n    \"command\": \"${input}\", \n    \"mode\": \"${mode}\",\n    \"username\": \"${username}\"\n}"
          clientActor ! ByteString(testString)
        }
      }
    }

  }
}