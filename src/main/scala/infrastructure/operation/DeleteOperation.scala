package infrastructure.operation

import org.json.simple.JSONObject

class DeleteOperation(val position:Int, val length:Int) extends Operation {

  def apply(document:String) = {
    if (document.length < (position + length)) {
		throw new Exception("Document length mismatch in delete operation.")
	}
	
	if (position == 0)
	  	document.substring(length)
	else
	  	document.substring(0, position) + document.substring(position + length)
  }
  
  override def clone() = new DeleteOperation(position, length)
  
  override def toString() = "Delete Operation: position = " + position + " length = " + length
}

object DeleteOperation {
	def deserialize(o:JSONObject) = {
		new DeleteOperation(o.get("position").toString.toInt, o.get("length").toString.toInt)
	}
}
