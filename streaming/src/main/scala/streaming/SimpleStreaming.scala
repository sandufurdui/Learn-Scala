package streaming

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.Future

object SimpleStreaming extends ExtraStreamOps with SimpleStreamingInterface:

  def mapToStrings(ints: Source[Int, NotUsed]): Source[String, NotUsed] =
    ints.map { _.toString }

  def filterEvenValues: Flow[Int, Int, NotUsed] =
    Flow[Int].filter { _ % 2 == 0 }

  def filterUsingPreviousFilterFlowAndMapToStrings(ints: Source[Int, NotUsed]): Source[String, NotUsed] =
    mapToStrings(ints.via(filterEvenValues))


  def filterUsingPreviousFlowAndMapToStringsUsingTwoVias(ints: Source[Int, NotUsed], toString: Flow[Int, String, _]): Source[String, NotUsed] =
    ints.via(filterEvenValues).via(toString)


  def firstElementSource(ints: Source[Int, NotUsed]): Source[Int, NotUsed] =
    ints.take(1)


  def firstElementFuture(ints: Source[Int, NotUsed])(using Materializer): Future[Int] =
    ints.runWith(Sink.head)

  // --- failure handling ---


  def recoverSingleElement(ints: Source[Int, NotUsed]): Source[Int, NotUsed] =
    ints.recover { case _: IllegalStateException => -1 }

  /**
   * Recover [[IllegalStateException]] values to the provided fallback Source
   *
   */
  def recoverToAlternateSource(ints: Source[Int, NotUsed], fallback: Source[Int, NotUsed]): Source[Int, NotUsed] =
    ints.recoverWithRetries(-1, { case _: IllegalStateException => fallback} )

  // working with rate


  def sumUntilBackpressureGoesAway: Flow[Int, Int, _] = Flow[Int].conflate { (e1, e2) => e1 + e2 }


  /**
   * See also [[Iterator.continually]]
   */
  def keepRepeatingLastObservedValue: Flow[Int, Int, _] = Flow[Int].extrapolate { v => Iterator.continually(v) }


