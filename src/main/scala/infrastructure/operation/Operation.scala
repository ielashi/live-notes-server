package infrastructure.operation

trait Operation {
	def apply(document:String) : String
}
