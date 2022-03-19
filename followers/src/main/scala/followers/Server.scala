package followers

import akka.{NotUsed, util}
import akka.event.Logging
import akka.stream.scaladsl.{BroadcastHub, Flow, Framing, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorAttributes, Materializer}
import akka.util.ByteString
import followers.Server.{eventParserFlow, followersFlow, isNotified, reintroduceOrdering}
import followers.model.{Event, Followers, Identity}

import scala.collection.immutable.SortedSet
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters._

/**
  * Utility object that describe stream manipulations used by the server
  * implementation.
  */
object Server extends ServerModuleInterface:

  /**
    * A flow that consumes chunks of bytes and produces `String` messages.
    *
    * Each incoming chunk of bytes doesn’t necessarily contain ''exactly one'' message
    * payload (it can contain fragments of payloads only). You have to process these
    * chunks to produce ''frames'' containing exactly one message payload.
    *
    * Messages are delimited by the '\n' character.
    *
    * If the last frame does not end with a delimiter, this flow should fail the
    * stream instead of returning a truncated frame.
    *
    * Hint: you may find the [[Framing]] flows useful.
    */
  val reframedFlow: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 256).map { _.utf8String }

  /**
    * A flow that consumes chunks of bytes and produces [[Event]] messages.
    *
    * Each incoming chunk of bytes doesn't necessarily contain exactly one message payload (it
    * can contain fragments of payloads only). You have to process these chunks to produce
    * frames containing exactly one message payload before you can parse such messages with
    * [[Event.parse]].
    *
    * Hint: reuse `reframedFlow`
    */
  val eventParserFlow: Flow[ByteString, Event, NotUsed] = reframedFlow.map { Event.parse }

  /**
    * Implement a Sink that will look for the first [[Identity]]
    * (we expect there will be only one), from a stream of commands and materializes a Future with it.
    *
    * Subsequent values once we found an identity can be ignored.
    *
    * Note that you may need the Sink's materialized value; you may
    * want to compare the signatures of `Flow.to` and `Flow.toMat`
    * (and have a look at `Keep.right`).
    */
  val identityParserSink: Sink[ByteString, Future[Identity]] =
    reframedFlow.map { Identity.parse }.toMat(Sink.head)(Keep.right)

  /**
    * A flow that consumes unordered messages and produces messages ordered by `sequenceNr`.
    *
    * User clients expect to be notified of events in the correct order, regardless of the order in which the
    * event source sent them.
    *
    * You will have to buffer messages with a higher sequence number than the next one
    * expected to be produced. The first message to produce has a sequence number of 1.
    *
    * You may want to use `statefulMapConcat` in order to keep the state needed for this
    * operation around in the operator.
    */
  val reintroduceOrdering: Flow[Event, Event, NotUsed] = Flow[Event].statefulMapConcat { () =>
    var sequenceNumber = 1
    var buffer = List.empty[Event]

    event =>
      if (sequenceNumber == event.sequenceNr) {
        val orderedEvents = (event :: buffer).sortWith((e1, e2) => e1.sequenceNr < e2.sequenceNr)
        sequenceNumber = orderedEvents.last.sequenceNr + 1
        buffer = Nil
        orderedEvents
      } else {
        buffer = event :: buffer
        Nil
      }
  }

  /**
    * A flow that associates a state of [[Followers]] to
    * each incoming [[Event]].
    *
    * Hints:
    *  - start with a state where nobody follows nobody,
    *  - you may find the `statefulMapConcat` operation useful.
    */
  val followersFlow: Flow[Event, (Event, Followers), NotUsed] = Flow[Event].statefulMapConcat { () =>
    var followersMemory: Followers = Map.empty

    event => {
      event match {
        case f @ Event.Follow(_, fromUserId, toUserId) =>
          followersMemory.get(fromUserId) match {
            case Some(followings) => followersMemory += (fromUserId -> (followings + toUserId))
            case None => followersMemory += (fromUserId -> Set(toUserId))
          }
        case u @ Event.Unfollow(_, fromUserId, toUserId) =>
          followersMemory.get(fromUserId).foreach { followings => followersMemory += (fromUserId -> (followings - toUserId)) }
        case _ @ (Event.PrivateMsg(_, _, _) | Event.StatusUpdate(_, _) | Event.Broadcast(_)) =>
      }

      List((event, followersMemory))
    }
  }

  /**
    * @return Whether the given user should be notified by the incoming `Event`,
    *         given the current state of `Followers`. See [[Event]] for more
    *         information of when users should be notified about them.
    * @param userId            Id of the user
    * @param eventAndFollowers Event and current state of followers
    */
  def isNotified(userId: Int)(eventAndFollowers: (Event, Followers)): Boolean =
    eventAndFollowers match {
      case (Event.Follow(_, _, toUserId), _) => userId == toUserId
      case (Event.Unfollow(_, _, _), _) => false
      case (Event.PrivateMsg(_, _, toUserId), _) => userId == toUserId
      case (Event.Broadcast(_), _) => true
      case (Event.StatusUpdate(_, fromUserId), followers: Followers) => followers.getOrElse(userId, Set.empty).contains(fromUserId)
    }

  // Utilities to temporarily have unimplemented parts of the program
  private def unimplementedFlow[A, B, C]: Flow[A, B, C] =
    Flow.fromFunction[A, B](_ => ???).mapMaterializedValue(_ => ??? : C)

  private def unimplementedSink[A, B]: Sink[A, B] = Sink.ignore.mapMaterializedValue(_ => ??? : B)

/**
  * Creates a hub accepting several client connections and a single event connection.
  *
  * @param executionContext Execution context for `Future` values transformations
  * @param materializer     Stream materializer
  */
class Server(using ExecutionContext, Materializer) extends ServerInterface with ExtraStreamOps :

  import Server.*


  val (inboundSink, broadcastOut) =
    /**
      * A flow that consumes the event source, re-frames it,
      * decodes the events, re-orders them, and builds a Map of
      * followers at each point it time. It produces a stream
      * of the decoded events associated with the current state
      * of the followers Map.
      */
    val incomingDataFlow: Flow[ByteString, (Event, Followers), NotUsed] = eventParserFlow.via(reintroduceOrdering).via(followersFlow)

    // Wires the MergeHub and the BroadcastHub together and runs the graph
    MergeHub.source[ByteString](256)
      .via(incomingDataFlow)
      .toMat(BroadcastHub.sink(256))(Keep.both)
      .withAttributes(ActorAttributes.logLevels(Logging.DebugLevel, Logging.DebugLevel, Logging.DebugLevel))
      .run()


  val eventsFlow: Flow[ByteString, Nothing, NotUsed] =
    Flow.fromSinkAndSourceCoupled(inboundSink, Source.maybe)

  def outgoingFlow(userId: Int): Source[ByteString, NotUsed] =
    broadcastOut
      .filter { isNotified(userId)(_) }
      .map { (event: Event, _) => event.render }


  def clientFlow(): Flow[ByteString, ByteString, NotUsed] =
    val clientIdPromise = Promise[Identity]()
    val incoming: Sink[ByteString, NotUsed] =
      Flow[ByteString].to(identityParserSink.mapMaterializedValue(Id => clientIdPromise.completeWith(Id)))

    val outgoing = Source.futureSource(clientIdPromise.future.map { identity => outgoingFlow(identity.userId) })

    Flow.fromSinkAndSource(incoming, outgoing)