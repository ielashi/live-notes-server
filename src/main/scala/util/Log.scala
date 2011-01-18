package util

object Log {
  val debug:Boolean = true

  def info(message:String) = if (debug) println(message)

  def error(message:String) = if (debug) println(message)
}
