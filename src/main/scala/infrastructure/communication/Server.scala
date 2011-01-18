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

import infrastructure.data.{Cleanup, Document}
import name.fraser.neil.plaintext._ 
import org.apache.commons.codec.digest.DigestUtils
import org.json.simple.parser.JSONParser
import org.json.simple.JSONObject
import org.xsocket.connection.{INonBlockingConnection, IServer}
import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.HashMap
import util.Log


object Server extends Actor {
  private val id = 0
  private val diffPatch = new diff_match_patch() // for diff and patch
  private val logPrefix = "[Server] " // for logging
  private val jsonParser = new JSONParser
  private var socketConnection:IServer = null

  /**
   * Starts a socket connection listening on a given port.
   *
   * Args:
   *   port: port number to listen on.
   */
  def connect(port:Int) = {
    // Start a socket connection. All incoming data is sent to DataReceiver
    socketConnection = new org.xsocket.connection.Server(port, new DataReceiver)
    socketConnection run()
    shutdownServer
  }


  /**
   * Handle messages received by clients.
   */
  def act = {
    loop {
      receive {
        case (msg : Message, connection:INonBlockingConnection) => {
          receiveMessage(msg, connection)
        }
        case Client.cleanupMessage => Client cleanup
        case Document.cleanupMessage => Document cleanup
        case (connectionData: String, connection: INonBlockingConnection) => {
          connectClient(connectionData, connection)
        }
        case _ => Log.error(logPrefix + "unrecognized message received")
      }
    }
  }

  /**
   * Update the server's copy of the document by patching in the changes done
   * by a client.
   *
   * Args:
   *   documentName: Name of the document to patch.
   *   patch: The patch to apply to the document.
   */
  def patchServer(documentName: String, patch: String) {
    val patchObjects = diffPatch.patch_fromText(patch)
    val result = diffPatch.patch_apply(patchObjects,
        document(documentName) text)

    document(documentName) text = result(0).asInstanceOf[String]

    Log.info(logPrefix + "server document digest: "
        + DigestUtils.shaHex(document(documentName) text))
  }

  /**
   * Handle messages from clients.
   *
   * Args:
   *   clientMessage: Message received from a client.
   */
  def receiveMessage(clientMessage: Message,
      connection: INonBlockingConnection):Unit = {
    Client client(clientMessage.senderId) match {
      case Some(client) => {
        if (clientMessage.patchData != "") {
          // Patch server shadow of the client
          val clientPatchedSuccessfully = client patch(clientMessage.patchData,
              clientMessage.checksum)
  
           if (!clientPatchedSuccessfully) {
            Log.error(logPrefix + "oh oh! wrong checksum. panicking...")
            Log.error("Client document: " + client.shadow)
            client.refresh(connection)
            return
          }

          // Patch server document
           patchServer(clientMessage.documentName, clientMessage.patchData)
        }
          
        sendReply(client, connection)
      }
      case _ => {
        // If the client is not found, reinitialize the client
        Client.add(clientMessage.senderId, document(clientMessage.documentName),
            clientMessage.conType).sendDocument(connection)
      }
    }
  }

  /**
   * Calculate the diffs between the client shadow and the current server
   * document and send it to the client.
   * Assumes that the client shadow is up to date and passed the checksum test.
   */
  def sendReply(client:Client, connection:INonBlockingConnection) = {
    // a snapshot of the server document
    val serverDocument = new String(client.document.text)

    val diffs = diffPatch.diff_main(client shadow, serverDocument)

    client shadow = serverDocument

    val patchObjects = diffPatch.patch_make(diffs)

    val patches = diffPatch.patch_toText(patchObjects)

    val checksum = DigestUtils.shaHex(client shadow)

    val messageToSend = new Message(id, client.document.name, patches, checksum,
        client.connectionType)

    if (messageToSend.patchData != "") {
      Log.info(logPrefix + "sending to client " + client.id + ": "
          + messageToSend.toJson)
    }
    Server send("m," + messageToSend.toJson, connection, client.connectionType)
  }


  /**
   * Send message to a client.
   */
  def send(message:String, connection:INonBlockingConnection,
      connectionType:String) = {
    try {
      connectionType match {
        case "xmlsocket" =>  {
          connection write(message + "\0")
        }
        case _ => {
          connection write(message)
            connection close
        }
      }
    } catch { 
      case e: Exception => Log.error(e.getStackTraceString)
      case _ => {
        Log.error("Error sending message. Disconnecting...")
        connection close
      }
    }
  }

  def document(documentName : String) = {
    Document.document(documentName)
  }

  def shutdownServer() = {
    socketConnection close
  }
  
  def connectClient(clientData:String, clientStream : INonBlockingConnection) {
    val parsedClientData =
        jsonParser.parse(clientData).asInstanceOf[JSONObject];

    if (parsedClientData.get("connectionType") == null) {
      Log.error("No connection type is specified. Disconnecting.")
      clientStream.close
      return
    }

    if (parsedClientData.get("documentName") == null) {
      Log.error("Document name not found in client's connection request")
      clientStream.close
      return
    }

    val connectionType = parsedClientData.get("connectionType").toString

    val documentName = parsedClientData.get("documentName").toString

    // Store the client connection and generate a client id
    val client = Client add(document(documentName), connectionType) 

    client sendDocument(clientStream)
  }
}

object Main {
  /**
   * Main entry point.
   */
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Args: port-number")
      return
    }

    val portNumber: Int = args(0).toInt

    Server start()
    Cleanup start()
    
    Server connect(portNumber)
  }
}
