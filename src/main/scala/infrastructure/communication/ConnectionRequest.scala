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

class ConnectionRequest(val id: Int, val initialDocument: String,
    val connectionType:String) {
  def toJson = ConnectionRequest.jsonBuilder.toJson(this)
}

object ConnectionRequest {
  val jsonBuilder = new Gson()  // used to convert objects to JSON
}
