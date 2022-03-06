package protocols

import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist.Find
import akka.actor.typed.scaladsl._

import scala.concurrent.duration._

object Transactor:

  sealed trait PrivateCommand[T] extends Product with Serializable
  final case class Committed[T](session: ActorRef[Session[T]], value: T) extends PrivateCommand[T]
  final case class RolledBack[T](session: ActorRef[Session[T]]) extends PrivateCommand[T]

  sealed trait Command[T] extends PrivateCommand[T]
  final case class Begin[T](replyTo: ActorRef[ActorRef[Session[T]]]) extends Command[T]

  sealed trait Session[T] extends Product with Serializable
  final case class Extract[T, U](f: T => U, replyTo: ActorRef[U]) extends Session[T]
  final case class Modify[T, U](f: T => T, id: Long, reply: U, replyTo: ActorRef[U]) extends Session[T]
  final case class Commit[T, U](reply: U, replyTo: ActorRef[U]) extends Session[T]
  final case class Rollback[T]() extends Session[T]

  def apply[T](value: T, sessionTimeout: FiniteDuration): Behavior[Command[T]] =
    SelectiveReceive(30, idle(value, sessionTimeout)).narrow[Command[T]]

  private def idle[T](value: T, sessionTimeout: FiniteDuration): Behavior[PrivateCommand[T]] = Behaviors.receive {
    case (ctx, Begin(requester)) =>
      val childSessionHandler = ctx.spawnAnonymous(sessionHandler(value, ctx.self, Set.empty[Long]))
      requester ! childSessionHandler
      ctx.watchWith(childSessionHandler, RolledBack(childSessionHandler))
      inSession(value, sessionTimeout, childSessionHandler)
    case (_, RolledBack(childSessionRef)) =>
      childSessionRef ! Rollback()
      Behaviors.same
    case (_, Committed(_, _)) => Behaviors.same
  }

  private def inSession[T](rollbackValue: T, sessionTimeout: FiniteDuration, sessionRef: ActorRef[Session[T]]): Behavior[PrivateCommand[T]] =
    Behaviors.setup { ctx =>
      ctx.setReceiveTimeout(sessionTimeout, RolledBack(sessionRef))

      Behaviors.receivePartial {
        case (_, Committed(_, updatedValue)) => idle(updatedValue, sessionTimeout)
        case (ctx, RolledBack(childSessionRef)) =>
          ctx.stop(childSessionRef)
          idle(rollbackValue, sessionTimeout)
      }
    }

  private def sessionHandler[T](currentValue: T, commit: ActorRef[Committed[T]], done: Set[Long]): Behavior[Session[T]] =
    Behaviors.receive {
      case (_, Extract(fn, replyTo)) =>
        replyTo ! fn(currentValue)
        Behaviors.same
      case (_, Modify(fn, id, reply, replyTo)) =>
        done.contains(id) match {
          case true =>
            replyTo ! reply
            Behaviors.same
          case false =>
            replyTo ! reply
            sessionHandler(fn(currentValue), commit, done + id)
        }
      case (ctx, Commit(reply, replyTo)) =>
        replyTo ! reply
        commit ! Committed(ctx.self, currentValue)
        Behaviors.stopped
      case (_, Rollback()) => Behaviors.stopped
    }