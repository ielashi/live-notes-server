import sbt._

class LiveNotesServerProject(info: ProjectInfo) extends DefaultProject(info) {
  val jsonSimple = "json-simple" % "json-simple" % "1.1" from "http://json-simple.googlecode.com/files/json_simple-1.1.jar"
} 
