package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import scala.concurrent.duration.*


object Replicator:
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)
  
  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  case object RetrySnap

  def props(replica: ActorRef): Props = Props(Replicator(replica))

class Replicator(val replica: ActorRef) extends Actor:
  import Replicator.*
  import context.dispatcher
  
  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]
  
  var _seqCounter = 0L
  def nextSeq() =
    val ret = _seqCounter
    _seqCounter += 1
    ret

  
  /* TODO Behavior for the Replicator. */
//  def receive: Receive =
//    case _ =>

  def receive: Receive = {
    case rep @ Replicate(key, valueOpt, id) => {
      val seq = nextSeq()
      acks += seq -> (sender(), rep)
      replica ! Snapshot(key, valueOpt, seq)
    }
    case SnapshotAck(key, seq) => {
      acks.get(seq).map{entry =>
        val (primary, command) = entry
        primary ! Replicated(key, command.id)
      }
      acks -= seq
    }
    case RetrySnap => {
      acks.foreach(entry => {
        val (seq, (primary, replicate)) = entry
        replica ! Snapshot(replicate.key, replicate.valueOption, seq)
      })
    }
  }

