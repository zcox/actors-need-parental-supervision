package iascala

/*
One actor generates a number every n msec and sends it to the next ActorRef
Another actor receives a number, and either:
  - logs it
  - or throws an exception
The logging actor's supervisor restarts it on the exception

Things to note:
  - supervisor creates the child
  - child throws the exception, but supervisor receives it & determines what happens to child
  - supervisor gets the message the child was processing when exception was thrown
  - number sending actor never knows about exception, restarting, new child instance, etc
  - even when using system.actorOf, the actor is still supervised
*/

import akka.actor._
import akka.util.duration._
import scala.util.Random

object BasicMain extends App {
  import NumberGenerator._

  unsupervised(ActorSystem("basic"))

  def unsupervised(system: ActorSystem) {
    val generator = system.actorOf(Props[NumberGenerator], "generator")
    val logger = system.actorOf(Props(new NumberLogger(3)), "logger")
    system.scheduler.schedule(0 seconds, 1 second, generator, GenerateRandomNumber(5, logger))
  }
}

object NumberGenerator {
  case class GenerateRandomNumber(max: Int, next: ActorRef)
}

class NumberGenerator extends Actor with ActorLogging {
  import NumberGenerator._
  val random = new Random

  def receive = {
    case GenerateRandomNumber(max, next) => 
      val n = random nextInt max
      log.debug("Generated {}", n)
      next ! n
  }
}

class NumberLogger(bug: Int) extends Actor with ActorLogging {
  def receive = {
    case n: Int if n == bug => throw new IllegalStateException("There is a bug when handling number " + bug)
    case n: Int => log.debug("Processed {}", n)
  }
}
