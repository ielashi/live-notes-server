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
package infrastructure.communication

import infrastructure.data.Document
import java.sql.Timestamp
import name.fraser.neil.plaintext._
import org.apache.commons.codec.digest.DigestUtils
import org.xsocket.connection.INonBlockingConnection
import scala.collection.mutable.HashMap
import util.Log


class Client(val id:Int, val document:Document, val connectionType:String) {
  var shadow = new String(document.text)
  var backupShadow = new String(document.text)
  private var timestamp = Client currentTimestamp

  def logPrefix = "[Client " + id + "] "

  /**
   * Applies the provided patches to the document shadow of the client with
   * the id specified. It also verifies, through the checksum, that the patch
   * has been applied successfully.
   */
  def patch(patch:String, checksum:String):Boolean = {
    if (patch != "") {
      val patchObjects = Client.diffPatch.patch_fromText(patch)

      val result = Client.diffPatch.patch_apply(patchObjects, shadow)

      val patchedDocument = result(0).asInstanceOf[String]

      shadow = patchedDocument
    
      val resultChecksum = DigestUtils.shaHex(shadow)

      Log.info(logPrefix + "Document digest: " + resultChecksum)

      return (resultChecksum == checksum)
    } else {
      return true
    }
  }

  /**
   * In case the client goes out of sync with the server, revert the client's
   * document to that of the server.
   */
  def refresh(connection:INonBlockingConnection) = {
    Log.info(logPrefix + "refreshing.")
    shadow = new String(document.text)
    sendDocument(connection)
  }

  def sendDocument(connection:INonBlockingConnection) = {
    Log.info(logPrefix + "Sending document")
    val request = new ConnectionRequest(id, document.text, connectionType)
    Server send("c," + request.toJson, connection, connectionType)
  }

}

object Client {
  private val clients = new HashMap[Int, Client]

  private var idGenerator = 1

  private val logPrefix = "[Client Manager] "

  private val diffPatch = new diff_match_patch() // for diff and patch

  val cleanupMessage = "cleanup clients"

  // If clients lost pulse for more than this time, they will be removed
  val cleanupInterval = 5 * 60 * 1000  

  def client(i: Int):Option[Client] = {
    if (clients.contains(i)) {
      clients(i).timestamp = currentTimestamp
      Some(clients(i))
    } else {
      None
    }
  }

  /**
   * Adds a new instance of a client
   * returns: id of the new client created
   */
  def add(document: Document, connectionType: String): Client = {
    val clientId = idGenerator
    clients += clientId -> new Client(clientId, document, connectionType)
    idGenerator += 1
    clients(clientId)
  }

  /**
   * Adds a new instance of a client with a specific id
   */
  def add(clientId:Int, document:Document, connectionType:String):Client = {
    val client = new Client(clientId, document, connectionType)
    clients += clientId -> client 
    return client
  }

  def cleanup = {
    Log.info(logPrefix + "cleaning up")
    clients.values.foreach {
      client => if ((currentTimestamp - client.timestamp) > cleanupInterval) {
        clients.removeKey(client.id)
        Log.info(logPrefix + "removing client " + client.id)
      }
    }
  }

  def currentTimestamp = java.util.Calendar.getInstance.getTime.getTime
}
