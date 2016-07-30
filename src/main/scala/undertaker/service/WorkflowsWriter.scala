package undertaker.service

import undertaker.Workflow
import scala.concurrent.Future

trait WorkflowsWriter {
  def write(workflow: Workflow): Workflow
}
