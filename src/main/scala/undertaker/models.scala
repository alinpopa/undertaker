package undertaker

case class ExecutionInfo(executionId: String, currentStep: Int, finished: Boolean = false)
case class Workflow(workflowId: String, steps: Int)
case class WorkflowExecutionId(executionId: String)
case class WorkflowRequest(steps: Int)
case class WorkflowResponse(workflowId: String)
case class FinishedInfo(finished: Boolean)
