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

  "Future from ask" should {
    "complete with a value" in {
      todo
    }

    "complete with an exception" in {
      todo
    }

    "complete with a timeout" in {
      todo
    }
  }

  step(system.shutdown())
}
