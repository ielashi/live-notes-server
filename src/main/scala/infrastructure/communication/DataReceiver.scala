package infrastructure.communication

import org.xsocket.connection.{IDataHandler, INonBlockingConnection}
import scala.util.matching.Regex
import util.Log


/**
 * A class receiving all incoming data from the socket connection
 * and forwarding it to the appropriate handlers.
 */
class DataReceiver extends IDataHandler {
  val logPrefix = "[Data Receiver] "
  val connectionRequest = new Regex("""c, (.*)""")
  val messageRequest = new Regex("""m, (.*)""")
  val policyRequest = new Regex("""<policy-file-request/>""")
  val securityPolicy: String = "<?xml version='1.0'?><cross-domain-policy>" +
      "<site-control permitted-cross-domain-policies='master-only'/>" +
      "<allow-access-from domain='*' to-ports='*'/>" +
      "</cross-domain-policy>"

  def onData(nbc: INonBlockingConnection) = {
    val data:String = nbc readStringByDelimiter "\0"

    data match {
      // For a connection request from a new client, forward the connection
      // data directly to the server.
      case connectionRequest(connectionData) => Server ! (connectionData, nbc)

      // For messages received from existing clients, parse it and forward
      // the message to the server.
      case messageRequest(messageData) => {
        Message.fromJson(messageData) match {
          case Some(message) => Server ! (message, nbc) 
          case _ => {
						Log.error(logPrefix + "unrecognized message: " + messageData) 
					}
        }
      }

      // For policy file requests needed by Adobe Flash's security, return an
      // XML string with the security settings.
      case "<policy-file-request/>"  => {
        Log.error(logPrefix + "received policy request.")
        Server send(securityPolicy, nbc, "xmlsocket")
      }
      
      case _ => Log.error(logPrefix + "unrecognized data: " + data)
    }
    
    true
  }
}

