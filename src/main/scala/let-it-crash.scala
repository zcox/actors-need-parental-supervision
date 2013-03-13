package iascala

import akka.actor.Actor
import java.sql.SQLException
import java.io.IOException

trait DangerousShit {
  def useTheDatabase() {
    //this could throw a nice variety of different exceptions
  }
}

class PleaseDontCrash extends Actor with DangerousShit {
  def receive = {
    case msg => try {
      useTheDatabase()
    } catch {
      case e: SQLException => //reconnect and try again?
      case e: IOException => //ummm... 
    }
  }
}

class GoAheadAndCrash extends Actor with DangerousShit {
  def receive = {
    case msg => useTheDatabase() //supervisor will deal with an exception
  }
}
