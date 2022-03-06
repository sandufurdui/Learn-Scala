package protocols

import akka.actor.typed.Behavior
import akka.actor.typed.Behavior.isUnhandled
import akka.actor.typed.scaladsl._

import scala.reflect.ClassTag

object SelectiveReceive:
  
  def apply[T: ClassTag](bufferCapacity: Int, initialBehavior: Behavior[T]): Behavior[T] =
    Behaviors.withStash[T](bufferCapacity) { buffer =>
      Behaviors.setup[T] { ctx =>
        intercept(bufferCapacity, buffer, Behavior.validateAsInitial(initialBehavior))
      }
    }

  
  private def intercept[T: ClassTag](bufferSize: Int, buffer: StashBuffer[T], started: Behavior[T]): Behavior[T] =
    Behaviors.receive {
      case (ctx, message) =>
        val nextBehavior = Behavior.interpretMessage(started, ctx, message)
        if (Behavior.isUnhandled(nextBehavior)) {
          buffer.stash(message)
          Behaviors.same
        } else buffer.unstashAll(SelectiveReceive(bufferSize, Behavior.canonicalize(nextBehavior, started, ctx)))
    }