package infrastructure.operation

import org.json.simple.JSONObject

class InsertOperation (val position: Int, val data: String) extends Operation  {

  // apply operation to document
  def apply(document:String) = {
    if (position == 0){
	  	data + document
		}
		else if (position == document.length()) {
			document + data
		}
		else {
			document.substring(0, position) + data + document.substring(position, document.length())
		}
  }
  
  override def clone() = new InsertOperation(position, data)
  
  override def toString() = "Insert Operation: position = " + position + " data = " + data
}

object InsertOperation {
	def deserialize(o:JSONObject) = {
		new InsertOperation(o.get("position").toString.toInt, o.get("data").toString)
	}
}
