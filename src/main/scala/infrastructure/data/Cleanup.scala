package infrastructure.data

import infrastructure.communication.{Client, Server}
import scala.actors.Actor
import scala.actors.Actor._

/**
 * Sends a signal to the server periodically to flush data of
 * disconnected clients.
 */
object Cleanup extends Actor {
  def act = {
    var cleanupCounter = 0
    loop {
      // check if no response from clients periodically
      Thread.sleep(Client cleanupInterval)
      Server ! Client.cleanupMessage
      cleanupCounter += 1

      if (cleanupCounter >= 12) {
        Server ! Document.cleanupMessage
        cleanupCounter = 0
      }
    }
  }
}
