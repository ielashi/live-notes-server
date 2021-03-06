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

import java.sql.Timestamp
import scala.collection.mutable.HashMap
import util.Log


/**
 * Document to be shared among different clients.
 */
class Document(val name:String) {
  var text = "" // TODO(ielashi): make it accessible only to Server

  var timestamp = Document currentTimestamp

  var patchHistory = List() // TODO(ielashi): change this to a linked list
}

object Document {
  private val documents = new HashMap[String, Document]

  val cleanupMessage = "cleanup documents"

  private val logPrefix = "[Document Manager] "

  val cleanupInterval = 60 * 60 * 1000 // a cleanup interval of one hour
 
  def document(documentName : String):Document = {
    val doc = documents.getOrElseUpdate(documentName, new Document(documentName))
    doc.timestamp = currentTimestamp
    return doc
  }

  def cleanup = {
    Log.info(logPrefix + "cleaning up")
    documents.values.foreach {
      document => if ((currentTimestamp - document.timestamp) > cleanupInterval) {
        documents.removeKey(document name)
        Log.info(logPrefix + "removing document " + document.name)
      }
    }
  }

  def currentTimestamp = java.util.Calendar.getInstance.getTime.getTime
}
