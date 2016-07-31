package undertaker.service

import undertaker.data.Models.Workflow

trait WorkflowReader {
  def read(workflowId: String): Option[Workflow]
}
