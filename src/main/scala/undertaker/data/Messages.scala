package undertaker.data

object Messages {
  import Models._

  object ExecutionAction {
    sealed trait ExecutionAction
    case object Run extends ExecutionAction
    case object GetState extends ExecutionAction
    case object Suicide extends ExecutionAction
  }

  object ExecutionState {
    sealed trait ExecutionState
    case class Running(currentStep: Int) extends ExecutionState
    case class Finished(currentStep: Int) extends ExecutionState
    case object MaxExecutionsReached extends ExecutionState
  }

  object WorkflowAction {
    sealed trait WorkflowAction
    case class New(workflowRequest: Models.WorkflowRequest) extends WorkflowAction
  }

  object WorkflowState {
    sealed trait WorkflowState
    case class Created(workflow: Workflow) extends WorkflowState
    case object MaxWorkflowsReached extends WorkflowState
  }
}
