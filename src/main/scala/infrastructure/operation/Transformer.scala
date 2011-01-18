package infrastructure.operation
import testing._

object Transformer {
/**
 * Transform the client and server operations C and S into C' and S' such that C + S' = S + C'
 */
  
  //def main(args:Array[String]) = InsertionTransformationTest testInsertionDeletionTransformation
    
  def transform(C:Operation, S:Operation) = {
    if (C.isInstanceOf[InsertOperation] && S.isInstanceOf[InsertOperation])
      transformInsertInsert(C.asInstanceOf[InsertOperation], S.asInstanceOf[InsertOperation])

    else if (C.isInstanceOf[InsertOperation] && S.isInstanceOf[DeleteOperation])
      transformInsertDelete(C.asInstanceOf[InsertOperation], S.asInstanceOf[DeleteOperation])
    
    else if (C.isInstanceOf[DeleteOperation] && S.isInstanceOf[InsertOperation])
      transformDeleteInsert(C.asInstanceOf[DeleteOperation], S.asInstanceOf[InsertOperation])
    
    else if (C.isInstanceOf[DeleteOperation] && S.isInstanceOf[DeleteOperation])
      transformDeleteDelete(C.asInstanceOf[DeleteOperation], S.asInstanceOf[DeleteOperation])
    
    else throw new Exception("Required transformation function is not implemented")
  }
    
  def transformInsertInsert(C:InsertOperation, S:InsertOperation) : (InsertOperation, InsertOperation) = {
    if (C.position < S.position)
      (C clone, new InsertOperation(S.position + C.data.length, S.data))
    else {
      (new InsertOperation(C.position + S.data.length, C.data), S clone)
    }
  }
  
  def transformDeleteDelete(C:DeleteOperation, S:DeleteOperation) : (DeleteOperation, DeleteOperation) = {
    if (C.position < S.position)
      (C clone, new DeleteOperation(S.position - C.length, S length))
    else if (C.position > S.position)
      (new DeleteOperation(C.position - S.length, C length), S clone)
    else {
      var newClientLength:Int = C length
      var newServerLength:Int = S length
      
      if (C.length > S.length) newClientLength = C.length - S.length else newClientLength = 0
      if (C.length < S.length) newServerLength = S.length - C.length else newServerLength = 0
     
      (new DeleteOperation(C position, newClientLength), new DeleteOperation(S position, newServerLength))
	}
  }
  
  def transformDeleteInsert(C:DeleteOperation, S:InsertOperation) : (DeleteOperation, InsertOperation) = {
    if (C.position < S.position) 
      (C clone, new InsertOperation(S.position - C.length, S.data)) 
    else 
      (new DeleteOperation(C.position + S.data.length, C.length), S)
  }
  
  def transformInsertDelete(C:InsertOperation, S:DeleteOperation) : (InsertOperation, DeleteOperation) = {
    val (sPrime, cPrime) = transformDeleteInsert(S, C)
    (cPrime, sPrime)
  }
}
