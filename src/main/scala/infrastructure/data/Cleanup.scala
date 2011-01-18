/**
 *  Copyright (C) 2011  Islam El-Ashi
 *  Email: ielashi@gmail.com
 *  Website: www.ielashi.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
