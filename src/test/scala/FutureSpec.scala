package iascala

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions
import akka.actor.ActorSystem
import akka.dispatch.{Future, Await, Promise}
import akka.util.duration._
import akka.testkit.{TestKit, ImplicitSender}
import java.util.concurrent.TimeoutException

//https://github.com/akka/akka/blob/master/akka-actor-tests/src/test/scala/akka/dispatch/FutureSpec.scala
class FutureSpec extends TestKit(ActorSystem("test")) with SpecificationLike with NoTimeConversions with ImplicitSender {
  sequential

  "Future.apply" should {
    "complete with a value" in {
      val future: Future[String] = Future { "done" }
      Await.result(future, 1 second) must_== "done"
      future.isCompleted must beTrue
      future.value must beSome(Right("done"))

      val future2: Future[String] = future map { _ + " and done"}
      Await.result(future2, 1 second) must_== "done and done"
    }

    "complete with an exception" in {
      val npe = new NullPointerException("fail")
      val future: Future[String] = Future { throw npe }
      Await.result(future, 1 second) must throwA(npe)
      future.isCompleted must beTrue
      future.value must beSome(Left(npe))

      val future2: Future[String] = future map { _ + " and done"}
      Await.result(future2, 1 second) must throwA(npe)
      future2.isCompleted must beTrue
      future.value must beSome(Left(npe))
    }

    "complete with a timeout" in { //note this is Await.result() timing out, not the future; this is different than an ask() timing out
      val future: Future[String] = Future { Thread.sleep(3000); "done" }
      Await.result(future, 1 second) must throwA[TimeoutException]

      val future2: Future[String] = future map { _ + " and done"}
      Await.result(future2, 1 second) must throwA[TimeoutException]
    }

    "recover from an exception" in {
      val future: Future[String] = Future { throw new NullPointerException("fail") } recover { case e: NullPointerException => "recovered" }
      Await.result(future, 1 second) must_== "recovered"
    }

    "fall back to a default value" in {
      val future: Future[String] = Future { throw new NullPointerException("fail") } fallbackTo Future("recovered") //or Promise.successful("recovered")
      Await.result(future, 1 second) must_== "recovered"
    }

    "not timeout" in {
      todo
    }
  }

  "Future.sequence" should {
    "complete with a seq of values if all of the futures complete with a value" in {
      val futures: Seq[Future[Int]] = (1 to 10) map { Future(_) }
      val future = Future.sequence(futures)
      Await.result(future, 1 second) must_== (1 to 10)
      future.isCompleted must beTrue
      future.value must beSome(Right((1 to 10)))
    }

    "complete with an exception if any one of the futures completes with an exception" in {
      val npe = new NullPointerException("fail")
      val futures: Seq[Future[Int]] = (1 to 10) map { 
        case 5 => Future(throw npe)
        case i => Future(i)
      }
      val future = Future.sequence(futures)
      Await.result(future, 1 second) must throwA(npe)
      future.isCompleted must beTrue
      future.value must beSome(Left(npe))
    }

    "recover from an exception if any one of the futures completes with an exception" in {
      val futures: Seq[Future[Int]] = (1 to 10) map { 
        case 5 => Future(throw new NullPointerException("fail"))
        case i => Future(i)
      } 
      val recovered = futures map { _ fallbackTo Future(-1) }
      val future = Future.sequence(recovered)
      Await.result(future, 1 second) must_== Seq(1,2,3,4,-1,6,7,8,9,10)
      future.isCompleted must beTrue
      future.value must beSome(Right(Seq(1,2,3,4,-1,6,7,8,9,10)))
    }
  }

  "Processing with futures" should {

    /*
    What should happen if stringFuture fails?
      - There is no string in stringFuture to operate on
      - The sizeFuture map will not be called
      - Let's fall back to a value of -1 for sizeFuture

    What should happen if sizeFuture fails?
      - If stringFuture contains a null, the _.size in map will fail with NPE
      - But then sizeFuture's fallbackTo will provide the default value
      - So sizeFuture will still succeed

    What should happen if the message future fails?
      - There could be a bug in the message-building code that would cause this future to fail
      - But then the fallbackTo will provide a generic error message
      - So should not be possible for this method to return a failed future
      - But if it did somehow return a future that will fail, it's entirely the caller's responsibility to handle it

    The fallbackTo or recover after a map will be used when either:
      - The original future fails
      - Or the future created by the map fails
    */
    val ErrorSize = -1
    val ErrorMessage = "Could not generate size message due to an error"
    def lengthMessage(stringFuture: Future[String]): Future[String] = {
      val sizeFuture = stringFuture map { _.size } fallbackTo { Promise.successful(ErrorSize) }
      sizeFuture map { "It had size " + _ } fallbackTo { Promise.successful(ErrorMessage) }
    }

    "stop if a required future fails" in {
      Await.result(lengthMessage(Promise.failed[String](new IllegalArgumentException("foo"))), 1 second) must_== "It had size -1"
      Await.result(lengthMessage(Promise.successful(null)), 1 second) must_== "It had size -1"
    }

    "continue if an optional future fails" in {
      todo
    }
  }

  "Future from ask" should {
    "complete with a value" in {
      todo
    }

    "fail because an exception was thrown" in {
      todo
    }

    "fail because the computation time exceeded the timeout" in {
      todo
    }

    "fail because no message was ever sent to sender" in {
      todo
    }
  }

  step(system.shutdown())
}
