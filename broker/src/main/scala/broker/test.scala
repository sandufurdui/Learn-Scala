package broker

import com.google.firebase.database._
import com.twitter.util.{Future, Promise}

import scala.beans.BeanProperty

case class MessageNotFoundException2(s: String) extends Exception(s)

object Message2 {
  def create(message2: Message2): Future[Message2] = {
    val ref = Firebase.ref(s"messages4/${message2.id}")
    val messageRecord2 = message2.toBean
    val p = new Promise[Message2]
    ref.setValue(messageRecord2, new DatabaseReference.CompletionListener() {
      override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference) {
        if (databaseError != null) {
          p.setException(new FirebaseException(databaseError.getMessage()))
        } else {
          p.setValue(message2)
        }
      }
    })
    p
  }
  def get(id: Int): Future[Message2] = {
    val ref = Firebase.ref(s"messages4/$id")
    val p = new Promise[Message2]
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
      override def onDataChange(snapshot: DataSnapshot) = {
        val messageRecord2: MessageBean2 = snapshot.getValue(classOf[MessageBean2])
        if (messageRecord2 != null) {
          p.setValue(messageRecord2.toCase)
        } else {
          p.setException(new MessageNotFoundException2(s"main.Message $id not found."))
        }
      }
      override def onCancelled(databaseError: DatabaseError) = {
        p.setException(new FirebaseException(databaseError.getMessage()))
      }
    })
    p
  }
}

case class Message2(id: Int, reference: String, list: List[Int]) {
  def toBean = {
    val message2 = new MessageBean2()
    message2.id = id
    message2.reference = reference
    message2.list = list
    message2
  }
}

/** Plain class required for parsing main.Firebase DataSnapshot */
class MessageBean2()  {
  type T = String
  @BeanProperty var id: Int = 0
  @BeanProperty var reference: String = null
  @BeanProperty var list: List[Int] = null

  def toCase: Message2 = {
    Message2(id, reference, list)
  }
  override def toString = s"$id: $reference, $list"
}