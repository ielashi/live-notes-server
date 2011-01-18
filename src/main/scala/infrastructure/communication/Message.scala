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

import com.google.gson.Gson
import org.json.simple.parser.JSONParser
import org.json.simple.JSONObject
import util.Log


class Message(val senderId:Int, val documentName:String, val patchData:String,
    val checksum:String, val conType:String) {

  override def toString = {
    "Message: " + "sender id: " + senderId + ", document name: " +
      documentName + ", patch data: " + patchData + ", checksum: " + checksum
  }

  def toJson = Message.jsonBuilder.toJson(this)
}


object Message { 
  val jsonBuilder = new Gson()  // used to convert objects to JSON
  val jsonParser = new JSONParser

  val logPrefix = "[Message] " // for logging

  def fromJson(jsonData : String):Option[Message] = {
    try {
      deserialize(jsonParser.parse(jsonData).asInstanceOf[JSONObject])
    } catch {
      case _ => return None
    }
  }

  def deserialize(o:JSONObject):Option[Message] = {
    var patchData:String = ""; var checksum:String = "";

    if (o.get("patchData") != null)  patchData = o.get("patchData").toString
    if (o.get("checksum") != null)  checksum = o.get("checksum").toString

    Some(new Message(o.get("senderId").toString.toInt,
        o.get("documentName").toString, patchData, checksum,
        o.get("conType").toString))
  }
}
