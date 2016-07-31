package undertaker.service

import undertaker.data.Models.Workflow

trait WorkflowsWriter {
  def write(workflow: Workflow): Workflow
}
