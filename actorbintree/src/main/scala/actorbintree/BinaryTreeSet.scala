package actorbintree

import akka.actor._
import scala.collection.immutable.Queue

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection*/
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}


class BinaryTreeSet extends Actor {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {
    case Contains(r, qid, v) => root ! Contains(r, qid, v)
    case Insert(r, qid, v) => root ! Insert(r, qid, v)
    case Remove(r, qid, v) => root ! Remove(r, qid, v)

  }
}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode],  elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case Contains(requester, qid, value) => {
      // println(s"$elem) Contains qid= $qid, value=$value")
      if((elem == value) && !removed) requester ! ContainsResult(qid, true)
      else if(subtrees.isEmpty) requester ! ContainsResult(qid, false)
      else if(value < elem && subtrees.contains(Left)) subtrees(Left) ! Contains(requester, qid, value)
      else if(value > elem && subtrees.contains(Right)) subtrees(Right) ! Contains(requester, qid, value)
      else requester ! ContainsResult(qid, false)
    }
    case Insert(requester, qid, value) => {
      println(s"$elem) insert qid= $qid, value=$value")
      def addTo(p : Position) = {
        subtrees.get(p) match {
          case Some(a) => a ! Insert(requester, qid, value)
          case _ => {
            subtrees = subtrees + (p -> context.actorOf(BinaryTreeNode.props(value, false), value.toString()))
            requester ! OperationFinished(qid)
          }
        }
      }
      if(elem == value && removed) { removed = false; requester ! OperationFinished(qid) }
      else if(value < elem) addTo(Left)
      else if(value > elem) addTo(Right)
      else requester ! OperationFinished(qid)
    }
    case Remove(requester, qid, value) => {
      println(s"$elem) delete qid= $qid, value=$value")
      def sendRemoveTo(p: Position) = subtrees.get(p).get ! Remove(requester, qid, value)

      if(elem == value) { removed = true; requester ! OperationFinished(qid) }
      else if((value < elem) && (subtrees.isDefinedAt(Left))) sendRemoveTo(Left)
      else if((value > elem) && (subtrees.isDefinedAt(Right))) sendRemoveTo(Right)
      else requester ! OperationFinished(qid)
    }
  }
}