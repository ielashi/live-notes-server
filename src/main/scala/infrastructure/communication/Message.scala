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

	  if (o.get("patchData") != null)	patchData = o.get("patchData").toString
	  if (o.get("checksum") != null)	checksum = o.get("checksum").toString

		Some(new Message(o.get("senderId").toString.toInt,
        o.get("documentName").toString, patchData, checksum,
        o.get("conType").toString))
  }
}
