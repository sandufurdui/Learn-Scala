package broker

import scala.beans.BeanProperty
import com.twitter.util.{Future, Promise}
import com.google.firebase.database._

case class MessageNotFoundException(s: String) extends Exception(s)

object Message {
  def create(message: Message): Future[Message] = {
    val ref = Firebase.ref(s"${message.reff}/${message.id}")
    val messageRecord = message.toBean
    val p = new Promise[Message]
    ref.setValue(messageRecord, new DatabaseReference.CompletionListener() {
      override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference) {
        if (databaseError != null) {
          p.setException(new FirebaseException(databaseError.getMessage()))
        } else {
          p.setValue(message)
        }
      }
    })
    p
  }

  def get(id: Int, reff: String): Future[Message] = {
    val ref = Firebase.ref(s"$reff/$id")
    val p = new Promise[Message]
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
      override def onDataChange(snapshot: DataSnapshot) = {
        val messageRecord: MessageBean = snapshot.getValue(classOf[MessageBean])
        if (messageRecord != null) {
          p.setValue(messageRecord.toCase)
        } else {
          p.setException(new MessageNotFoundException(s"Message $id not found."))
        }
      }
      override def onCancelled(databaseError: DatabaseError) = {
        p.setException(new FirebaseException(databaseError.getMessage()))
      }
    })
    p
  }
}

case class Message(id: Int, body: String, topic: String, reff: String) {
  def toBean = {
    val message = new MessageBean()
    message.id = id
    message.body = body
    message.topic = topic
    message.reff = reff
    message
  }
}

/** Plain class required for parsing Firebase DataSnapshot */
class MessageBean() {
  @BeanProperty var id: Int = 0
  @BeanProperty var body: String = null
  @BeanProperty var topic: String = null
  @BeanProperty var reff: String = null

  def toCase: Message = {
    Message(id, body, topic, reff)
  }
  override def toString = s"$body"
}
