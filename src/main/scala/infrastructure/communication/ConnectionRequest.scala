package infrastructure.communication

import com.google.gson.Gson

class ConnectionRequest(val id: Int, val initialDocument: String,
    val connectionType:String) {
  def toJson = ConnectionRequest.jsonBuilder.toJson(this)
}

object ConnectionRequest {
  val jsonBuilder = new Gson()  // used to convert objects to JSON
}
