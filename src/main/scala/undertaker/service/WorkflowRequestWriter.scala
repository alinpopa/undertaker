package undertaker.service

import undertaker.{Workflow, WorkflowRequest}
import scala.concurrent.Future

trait WorkflowRequestWriter {
  def write(workflowRequest: WorkflowRequest): Future[Workflow]
}
