package undertaker

sealed trait ExecutionMessage
case object Run extends ExecutionMessage
case object GetState extends ExecutionMessage
case object Suicide extends ExecutionMessage

sealed trait ExecutionState
case class Running(currentStep: Int) extends ExecutionState
case class Finished(currentStep: Int) extends ExecutionState
