package undertaker

case class ExecutionInfo(executionId: String, currentStep: Int, finished: Boolean = false)
case class Workflow(workflowId: String, steps: Int)
case class WorkflowRequest(steps: Int)
