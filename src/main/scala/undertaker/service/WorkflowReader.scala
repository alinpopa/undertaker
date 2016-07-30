package undertaker.service

import undertaker.Workflow

trait WorkflowReader {
  def read(workflowId: String): Option[Workflow]
}
